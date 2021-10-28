#   一款超强的文件选择框架。A super file picker framework.

[![](https://jitpack.io/v/ITxiaoguang/FilePicker.svg)](https://jitpack.io/#ITxiaoguang/FilePicker)

##  功能：
- 文件选择
- 支持多种类型文件选择
- 支持多个文件同时选择


## 如何添加
### Gradle添加：
#### 1.在Project的build.gradle中添加仓库地址

``` gradle
allprojects {
	repositories {
		...
		maven { url "https://jitpack.io" }
	}
}
```

#### 2.在Module目录下的build.gradle中添加依赖
[![](https://jitpack.io/v/ITxiaoguang/FilePicker.svg)](https://jitpack.io/#ITxiaoguang/FilePicker)
``` gradle
dependencies {
    implementation 'com.github.ITxiaoguang:FilePicker:xxx'
}
```

使用方法：

```java
    private ArrayList<String> docPaths = new ArrayList<>();

    private void filePicker() {
        String[] zips = {"zip", "rar"};
	String[] doc = {"doc", "docx"};
	String[] ppt = {"ppt", "pptx"};
	String[] pdf = {"pdf"};
	String[] txt = {"txt"};
	String[] apk = {"apk"};
	String[] xls = {"xls", "xlsx"};
	String[] music = {"m3u", "m4a", "m4b", "m4p", "ogg", "wma", "wmv", "ogg", "rmvb", "mp2", "mp3", "aac", "awb", "amr", "mka"};
	FilePickerBuilder.getInstance()
		.setMaxCount(9)// 最多选择
                .setSelectedFiles(docPaths)// 已选择文件地址
                .setActivityTheme(R.style.LibAppTheme)// 样式
		.enableCameraSupport(false)// 支持摄像头
		.showPic(true)// tab栏显示图片
		.showVideo(true)// tab栏显示视频
		.enableDocSupport(false)// 显示文档tab栏
		.addFileSupport("Word", doc, R.drawable.ic_file_word)// tab栏描述；类型；tab栏下item的图标
		.addFileSupport("压缩包", zips, R.drawable.ic_file_zip)
		.addFileSupport("PDF", pdf, R.drawable.ic_file_pdf)
		.addFileSupport("Txt文本", txt, R.drawable.ic_file_txt)
		.addFileSupport("PPT", ppt, R.drawable.ic_file_ppt)
		.addFileSupport("安装包", apk, R.drawable.ic_file_zip)
		.addFileSupport("Excel表格", xls, R.drawable.ic_file_excel)
		.addFileSupport("音乐", music, R.drawable.ic_file_music)
		.setActivityTitle("请选择文件")// 标题
		.sortDocumentsBy(SortingTypes.name)
		.withOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
		.pickFile(this, REQUEST_CODE_FILE);// 回调
    }
```

```java
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	if (resultCode == Activity.RESULT_OK) {
	    if (requestCode == REQUEST_CODE_FILE) {//选择文件类型回调
		docPaths.clear();
                ArrayList<String> filePaths = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS);
                docPaths.addAll(filePaths);
                Toast.makeText(this, filePaths.toString(), Toast.LENGTH_SHORT).show();
	    }
	}
    }
```
