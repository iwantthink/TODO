# 1.shape标签
shape 标签在 res/drawable 文件夹下

## 1.1 shape子标签属

- Corners . 定义四个角的圆角半径
		<corners    //定义圆角    
    	android:radius="dimension"      //全部的圆角半径    
    	android:topLeftRadius="dimension"   //左上角的圆角半径    
    	android:topRightRadius="dimension"  //右上角的圆角半径    
    	android:bottomLeftRadius="dimension"    //左下角的圆角半径    
    	android:bottomRightRadius="dimension" />    //右下角的圆角半径   

- solid 
		<solid  android:color="color" /> 


- gradient 
		<gradient   
    	android:type=["linear" | "radial" | "sweep"]    //共有3中渐变类型，线性渐变（默认）/放射渐变/扫描式渐变    
    	android:angle="integer"     //渐变角度，必须为45的倍数，0为从左到右，90为从上到下  仅对线性渐变有效 
    	android:centerX="float"     //渐变中心X的相当位置，范围为0～1  radial时有效 
    	android:centerY="float"     //渐变中心Y的相当位置，范围为0～1    
    	android:startColor="color"   //渐变开始点的颜色    
    	android:centerColor="color"  //渐变中间点的颜色，在开始与结束点之间    
    	android:endColor="color"    //渐变结束点的颜色    
    	android:gradientRadius="float"  //渐变的半径，只有当渐变类型为radial时才能使用    
    	android:useLevel=["true" | "false"] />  //使用LevelListDrawable时就要设置为true。设为false时才有渐变效果

- stroke
		<stroke         
    	android:width="dimension"   //描边的宽度    
    	android:color="color"   //描边的颜色    
    	// 以下两个属性设置虚线    
    	android:dashWidth="dimension"   //虚线的宽度，值为0时是实线    
    	android:dashGap="dimension" />      //虚线的间隔   

- size&padding  基本不用..控件本身也能实现
		<size    
    	android:width="dimension"    
    	android:height="dimension" /> 

		<padding     
    	android:left="dimension"    
    	android:top="dimension"    
    	android:right="dimension"    
    	android:bottom="dimension" />  

## 1.2 shape自身属性

	android:shape=["rectangle" | "oval" | "line" | "ring"]    
	shape的形状，默认为矩形，可以设置为矩形（rectangle）、椭圆形(oval)、线性形状(line)、环形(ring)    
	下面的属性只有在android:shape="ring时可用：    
	android:innerRadius         尺寸，内环的半径。    
	android:innerRadiusRatio    浮点型，以环的宽度比率来表示内环的半径，    
	android:thickness           尺寸，环的厚度    
	android:thicknessRatio      浮点型，以环的宽度比率来表示环的厚度，例如，如果android:thicknessRatio="2"，    
	android:useLevel            boolean值，如果当做是LevelListDrawable使用时值为true，否则为false. 

# 2.selector标签

# 3.layerlist 标签
- layerlist 可以设置drawable.shape和selector
- layerlist 是逐层绘制的，层与层之间不会互相影响

	<?xml version="1.0" encoding="utf-8"?>  
	<layer-list xmlns:android="http://schemas.android.com/apk/res/android">  
    	<item android:drawable="@drawable/blog1_1"/>  
    	<item android:drawable="@drawable/blog1_2"/>  
    	<item android:drawable="@drawable/blog1_3"/>  
	</layer-list>  

