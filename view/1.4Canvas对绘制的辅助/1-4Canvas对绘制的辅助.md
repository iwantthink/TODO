# 1 范围裁切
范围裁切有俩个方法:clipRect()和clipPath().裁切之后所执行的绘制代码都是在这个裁切范围之内。  
## 1.1clipRect()
	canvas.save();  
	canvas.clipRect(left, top, right, bottom);  
	canvas.drawBitmap(bitmap, x, y, paint);  
	canvas.restore();  

## 1.2clipPath()
	canvas.save();  
	canvas.clipPath(path1);  
	canvas.drawBitmap(bitmap, point1.x, point1.y, paint);  
	canvas.restore();
# 2 几何变换
几何变化的使用大概分成三类：  
1. 使用Canvas来做常见的二维变换  
2. 使用Matrix来做常见和不常见的二维变换  
3. 使用Camera来做三维变换  

## 2.1 使用Canvas 来做常见的二维变换  
需要注意的是：变换的代码是倒序来进行作用的。举个栗子,如下的代码，实际上**drawYYY的变换熟悉是先执行translate 再执行rotate** ！！drawXXX仍然只是rotate 之后 再进行绘制!!

	canvas.rotate(45);
	canvas.drawXXX
	canvas.translate(100,0);
	canvas.drawYYY


### 2.1.1 canvas.translate(float dx,float dy)平移 

### 2.1.2 canvas.rotate(float degrees,float px,float py)旋转  
degrees是旋转角度，方向是顺时针为正方向,px py 是轴心位置  
	
	canvas.save();  
	canvas.rotate(45, centerX, centerY);  
	canvas.drawBitmap(bitmap, x, y, paint);  
	canvas.restore();  

### 2.1.3 canvas.scale(float sx ,float sy,float px,float py) 缩放
sx,sy是横向和纵向的放缩总数，px py是轴心位置  

	canvas.save();  
	canvas.scale(1.3f, 1.3f, x + bitmapWidth / 2, y + bitmapHeight / 2);  
	canvas.drawBitmap(bitmap, x, y, paint);  
	canvas.restore();  

### 2.1.4 canvas.skew(float sx ,float sy)错切  
参数里的sx 和 sy是 x轴和y轴方向的错切系数  
[skew参考文章](http://www.jianshu.com/p/11e062284491 "skew 参考")


## 2.2 使用Matrix来做变换
[深入理解Android中的Matrix-参考文章](http://www.jianshu.com/p/6aa6080373ab)
### 2.2.0 Matrix说明
Matrix是Android SDK 提供的一个矩阵类（矩阵就是一个矩形阵列），代表一个3x3的矩阵

#### 2.2.0.1 getValues  



### 2.2.1 使用Matrix来做常见的变换
Matrix做常见变换的方式：  

1. 创建Matrix对象  
2. 调用Matrix的pre/postTranslate/Rotate/Scale/Skew方法来设置几何变换  
3. 使用Canvas.setMatrix(matrix) 或 canvas.concat(matrix)来把 matrix应用到canvas  

Matrix应用到canvas 有俩个方法:canvas.setMatrix canvas.concat
  
1. canvas.setMatrix:用Matrix直接替换canvas当前的变换矩阵,即抛弃canvas当前的变换,改用Matrix的变换  
2. canvas.concat:用canvas当前变换矩阵和Matrix相乘，即基于canvas当前的变换，叠加上Matrix中的变换  


### 2.2.2使用Matrix来做自定义变换
Matrix的自定义变换使用setPoyToPoly()方法  
#### 2.2.2.1 Matrix.setPolyToPoly(float[] src, int srcIndex, float[] dst, int dstIndex, int pointCount) 用点对点映射的方式设置变换  

poly 就是「多」的意思。setPolyToPoly() 的作用是通过多点的映射的方式来直接设置变换。「多点映射」的意思就是把指定的点移动到给出的位置，从而发生形变。例如：(0, 0) -> (100, 100) 表示把 (0, 0) 位置的像素移动到 (100, 100) 的位置，这个是单点的映射，单点映射可以实现平移。而多点的映射，就可以让绘制内容任意地扭曲。

	Matrix matrix = new Matrix();  
	float pointsSrc = {left, top, right, top, left, bottom, right, bottom};  
	float pointsDst = {left - 10, top + 50, right + 120, top - 90, left + 20, bottom + 30, right + 20, bottom + 60};

	...

	matrix.reset();  
	matrix.setPolyToPoly(pointsSrc, 0, pointsDst, 0, 4);

	canvas.save();  
	canvas.concat(matrix);  
	canvas.drawBitmap(bitmap, x, y, paint);  
	canvas.restore();  


参数里，src 和 dst 是源点集合目标点集；srcIndex 和 dstIndex 是第一个点的偏移；pointCount 是采集的点的个数（个数不能大于 4，因为大于 4 个点就无法计算变换了）。