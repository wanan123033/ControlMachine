package com.feipulai.host.view;
/**
 * 深圳市菲普莱体育发展有限公司		秘密级别：绝密
 */

import android.content.Context;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.feipulai.host.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileSelectView extends ListView implements OnItemClickListener{
	
	public static final String sRoot = Environment.getExternalStorageDirectory().getPath();
	public static final String sParent = "..";
	public static final String sFolder = ".";
	public static final String sEmpty = "";
	
	public static final String KEY_FILE_PATH = "path";
	public static final String KEY_FILE_NAME = "name";
	public static final String KEY_IMG = "image";
	public static final String SUFFIX_WAV = "wav";
	
	private static final String sOnErrorMsg = "No rights to access!";
	
	private onSelectListener listener;
	private String path = sRoot;
	private List<Map<String,Object>> list;
	private Map<String,Integer> images;
	
	public FileSelectView(Context context,AttributeSet attrs){
		super(context,attrs);
		this.setOnItemClickListener(this);
		//setFocusable(true);
		//setFocusableInTouchMode(true);
	}
	
	public void init(onSelectListener listener){
		this.listener = listener;
		
		// 下面几句设置各文件类型的图标， 需要你先把图标添加到资源文件夹
		images = new HashMap<String,Integer>();
		images.put(sRoot,R.drawable.filedialog_root);    //根目录图标
		images.put(sParent,R.drawable.filedialog_folder_up);    //返回上一层的图标
		images.put(sFolder,R.drawable.filedialog_folder);    //文件夹图标
		images.put(SUFFIX_WAV,R.drawable.filedialog_sorfile);    //wav文件图标
		images.put(sEmpty,R.drawable.filedialog_root);
		
		refreshFileList();
	}
	
	@Override
	public boolean onKeyUp(int keyCode,KeyEvent event){
		Log.e("james","what the fuck : " + keyCode);
		if(getVisibility() == VISIBLE && keyCode == KeyEvent.KEYCODE_BACK){
			File file = new File(path);
			//如果是根目录或者上一层
			String parentPath = file.getParent();
			if(!path.equals(sRoot) && parentPath != null){
				//返回上一层
				path = parentPath;
				refreshFileList();
			}else if(listener != null){
				listener.onFileSelected(null);
			}
			return true;
		}
		return super.onKeyUp(keyCode,event);
	}
	
	private String getSuffix(String filename){
		int dix = filename.lastIndexOf('.');
		return dix < 0 ? "" : filename.substring(dix + 1);
	}
	
	private int getImageId(String s){
		if(images == null){
			return 0;
		}else if(images.containsKey(s)){
			return images.get(s);
		}else if(images.containsKey(sEmpty)){
			return images.get(sEmpty);
		}else{
			return 0;
		}
	}
	
	private int refreshFileList(){
		File[] files = null;
		try{
			files = new File(path).listFiles();
		}catch(Exception e){
			files = null;
			Toast.makeText(getContext(),sOnErrorMsg,Toast.LENGTH_SHORT).show();
			return -1;
		}
		if(list != null){
			list.clear();
		}else{
			list = new ArrayList<Map<String,Object>>(files.length);
		}
		
		// 用来先保存文件夹和文件夹的两个列表
		ArrayList<Map<String,Object>> folderList = new ArrayList<Map<String,Object>>();
		ArrayList<Map<String,Object>> fileList = new ArrayList<Map<String,Object>>();
		if(!this.path.equals(sRoot)){
			//添加根目录或上一层目录
			Map<String,Object> map = new HashMap<String,Object>();
			map = new HashMap<String,Object>();
			map.put(KEY_FILE_NAME,sParent);
			map.put(KEY_FILE_PATH,path);
			map.put(KEY_IMG,getImageId(sParent));
			list.add(map);
		}
		
		for(File file : files){
			if(file.isDirectory() && file.listFiles() != null){
				// 添加文件夹
				Map<String,Object> map = new HashMap<String,Object>();
				map.put(KEY_FILE_NAME,file.getName());
				map.put(KEY_FILE_PATH,file.getPath());
				map.put(KEY_IMG,getImageId(sFolder));
				folderList.add(map);
			}else if(file.isFile()){
				//添加文件
				String sf = getSuffix(file.getName()).toLowerCase();
				if(sf.length() > 0){
					Map<String,Object> map = new HashMap<String,Object>();
					map.put(KEY_FILE_NAME,file.getName());
					map.put(KEY_FILE_PATH,file.getPath());
					map.put(KEY_IMG,getImageId(sf));
					fileList.add(map);
				}
			}
		}
		list.addAll(folderList); //先添加文件夹，确保文件夹显示在上面
		list.addAll(fileList);    //再添加文件
		
		SimpleAdapter adapter = new SimpleAdapter(getContext(),list,R.layout.filedialogitem,
				new String[]{KEY_IMG,KEY_FILE_NAME,KEY_FILE_PATH},
				new int[]{R.id.filedialogitem_img,R.id.filedialogitem_name,R.id.filedialogitem_path});
		
		this.setAdapter(adapter);
		return files.length;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent,View v,int position,long id){
		String filePath = (String)list.get(position).get(KEY_FILE_PATH);
		String fileName = (String)list.get(position).get(KEY_FILE_NAME);
		File file = new File(filePath);
		if(/*fileName.equals(sRoot) || */fileName.equals(sParent)){
			//如果是根目录或者上一层
			String parentPath = file.getParent();
			if(parentPath != null){
				//返回上一层
				path = parentPath;
			}else{
				//返回根目录
				path = sRoot;
			}
		}else{
			if(file.isFile()){
				listener.onFileSelected(filePath);
				return;
			}else if(file.isDirectory()){
				//如果是文件夹,那么进入选中的文件夹
				path = filePath;
			}
		}
		this.refreshFileList();
	}
	
}
