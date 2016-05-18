package download.otherFileLoader.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author MaTianyu
 * @date 14-11-7
 */
public class PackageUtil {
    /**
     * App installation location flags of android system
     */
    public static final int APP_INSTALL_AUTO = 0;
    public static final int APP_INSTALL_INTERNAL = 1;
    public static final int APP_INSTALL_EXTERNAL = 2;

    /**
     * 调用系统安装应用
     */
    public static boolean install(Context context, File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        return true;
    }

    /**
     * 调用系统卸载应用
     */
    public static void uninstallApk(Context context, String packageName) {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        Uri packageURI = Uri.parse("package:" + packageName);
        intent.setData(packageURI);
        context.startActivity(intent);
    }

    /**
     * 打开已安装应用的详情
     */
    public static void goToInstalledAppDetails(Context context, String packageName) {
        Intent intent = new Intent();
        int sdkVersion = Build.VERSION.SDK_INT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", packageName, null));
        } else {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra((sdkVersion == Build.VERSION_CODES.FROYO ? "pkg"
                    : "com.android.settings.ApplicationPkgName"), packageName);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    /**
     * 获取指定程序信息
     */
    public static ActivityManager.RunningTaskInfo getTopRunningTask(Context context) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            // 得到当前正在运行的任务栈
            List<ActivityManager.RunningTaskInfo> runningTasks = am.getRunningTasks(1);
            // 得到前台显示的任务栈
            ActivityManager.RunningTaskInfo runningTaskInfo = runningTasks.get(0);
            return runningTaskInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static int getAppVersionCode(Context context) {
        if (context != null) {
            PackageManager pm = context.getPackageManager();
            if (pm != null) {
                PackageInfo pi;
                try {
                    pi = pm.getPackageInfo(context.getPackageName(), 0);
                    if (pi != null) {
                        return pi.versionCode;
                    }
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }

    /**
     * 获取当前系统安装应用的默认位置
     *
     * @return APP_INSTALL_AUTO or APP_INSTALL_INTERNAL or APP_INSTALL_EXTERNAL.
     */
    public static int getInstallLocation() {
        ShellUtil.CommandResult commandResult = ShellUtil.execCommand(
                "LD_LIBRARY_PATH=/vendor/lib:/system/lib pm get-install-location", false, true);
        if (commandResult.result == 0 && commandResult.responseMsg != null && commandResult.responseMsg.length() > 0) {
            try {
                return Integer.parseInt(commandResult.responseMsg.substring(0, 1));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return APP_INSTALL_AUTO;
    }


    /**
     * get app package info
     */
    public static PackageInfo getAppPackageInfo(Context context) {
        if (context != null) {
            PackageManager pm = context.getPackageManager();
            if (pm != null) {
                PackageInfo pi;
                try {
                    return pm.getPackageInfo(context.getPackageName(), 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * whether context is system application
     */
    public static boolean isSystemApplication(Context context) {
        if (context == null) {
            return false;
        }
        return isSystemApplication(context, context.getPackageName());
    }

    /**
     * whether packageName is system application
     */
    public static boolean isSystemApplication(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null || packageName == null || packageName.length() == 0) {
            return false;
        }
        try {
            ApplicationInfo app = packageManager.getApplicationInfo(packageName, 0);
            return (app != null && (app.flags & ApplicationInfo.FLAG_SYSTEM) > 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取已安装的全部应用信息
     */
    public static List<android.content.pm.PackageInfo> getInsatalledPackages(Context context) {
        return context.getPackageManager().getInstalledPackages(0);
    }

    /**
     * 获取指定程序信息
     */
    public static ApplicationInfo getApplicationInfo(Context context, String pkg) {
        try {
            return context.getPackageManager().getApplicationInfo(pkg, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取指定程序信息
     */
    public static android.content.pm.PackageInfo getPackageInfo(Context context, String pkg) {
        try {
            return context.getPackageManager().getPackageInfo(pkg, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 启动应用
     */
    public static boolean startAppByPackageName(Context context, String packageName) {
        return startAppByPackageName(context, packageName, null);
    }

    /**
     * 启动应用
     */
    public static boolean startAppByPackageName(Context context, String packageName, Map<String, String> param) {
        android.content.pm.PackageInfo pi = null;
        try {
            pi = context.getPackageManager().getPackageInfo(packageName, 0);
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
                resolveIntent.setPackage(pi.packageName);
            }

            List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(resolveIntent, 0);

            ResolveInfo ri = apps.iterator().next();
            if (ri != null) {
                String packageName1 = ri.activityInfo.packageName;
                String className = ri.activityInfo.name;

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);

                ComponentName cn = new ComponentName(packageName1, className);

                intent.setComponent(cn);
                if (param != null) {
                    for (Map.Entry<String, String> en : param.entrySet()) {
                        intent.putExtra(en.getKey(), en.getValue());
                    }
                }
                context.startActivity(intent);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context.getApplicationContext(), "启动失败",
                    Toast.LENGTH_LONG).show();
        }
        return false;
    }
    public static String getString(Context context,String pkgName,Boolean detail) {
        StringBuffer sb = new StringBuffer();
        PackageManager pm = null;
        PackageInfo pkgInfo = null;
        if (pm == null) {
            pm = context.getPackageManager();
        }
        
        try {
            pkgInfo = pm.getPackageInfo(pkgName, PackageManager.GET_PERMISSIONS);
        } catch (NameNotFoundException e) {
        	e.printStackTrace();
        	return "";
        }
        
        String[] perms = pkgInfo.requestedPermissions;
        
        for (String permName: perms) {
            sb.append(permName).append('\n');
            if (detail) {
            	try {
            		PermissionInfo permInfo = pm.getPermissionInfo(permName, 0);
            		PermissionGroupInfo pgi = pm.getPermissionGroupInfo(permInfo.group, 0);
            		
            		sb.append(permInfo.loadLabel(pm)).append('\n');
            		sb.append(permInfo.loadDescription(pm)).append("\n\n");
            		
            		sb.append(pgi.loadLabel(pm)).append('\n');
            		sb.append(pgi.loadDescription(pm)).append("\n\n\n");
            		
            	} catch (NameNotFoundException e) {
            		sb.append("\n\n");
            	}
			}
        }
        return sb.toString();
    }
    public static void getSdLaunchPath(Context context,String name){
    	PackageManager pm = context.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(Environment.getExternalStorageDirectory()+File.separator+name, PackageManager.GET_ACTIVITIES);
		if (info != null) {
			Log.v("test", "包名："+info.packageName+"\n");
			ActivityInfo[] acinfos = info.activities;
			if (acinfos != null && acinfos.length > 0) {
				for (int j = 0; j < acinfos.length; j++) {
					if (acinfos[j].exported) {
						Log.v("test", "外部启动路径activity："+acinfos[j].name);
					}
				}
			}
			ServiceInfo[] svinfos = info.services;
			if (svinfos != null && svinfos.length > 0) {
				for (int j = 0; j < svinfos.length; j++) {
					if (svinfos[j].exported) {
						Log.v("test", "外部启动路径service："+svinfos[j].name);
					}
				}
			}
		}
    }
    public static String getApkName(Context context,String file){
    	PackageManager pm = context.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(file, PackageManager.GET_ACTIVITIES);
		if (info != null) {
			return info.packageName;
		}
		return "";
    }
}
