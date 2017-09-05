# 1 范围裁切
---
范围裁切有俩个方法:clipRect()和clipPath().裁切之后所执行的绘制代码都是在这个裁切范围之内。  
## 1.1clipRect()
	canvas.save();  
	canvas.clipRect(left, top, right, bottom);  
	canvas.drawBitmap(bitmap, x, y, paint);  
	canvas.restore();  

## 1.2clipPath()

canvas.clipPath(Path path,Op op),op参数 可以做一些取反的操作。。

	canvas.save();  
	canvas.clipPath(path1);  
	canvas.drawBitmap(bitmap, point1.x, point1.y, paint);  
	canvas.restore();



# 2 几何变换
---
几何变化的使用大概分成三类：  
1. 使用Canvas来做常见的二维变换  
2. 使用Matrix来做常见和不常见的二维变换  
3. 使用Camera来做三维变换  

## 2.1 使用Canvas 来做常见的二维变换  

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
- sx,sy是横向和纵向的放缩总数，px py是轴心位置  
- 默认的缩放中心为坐标原点，而缩放中心轴就是坐标轴    
- 当缩放比例为负数的时候会根据缩放中心轴进行翻转

	canvas.save();  
	canvas.scale(1.3f, 1.3f, x + bitmapWidth / 2, y + bitmapHeight / 2);  
	canvas.drawBitmap(bitmap, x, y, paint);  
	canvas.restore();  

### 2.1.4 canvas.skew(float sx ,float sy)错切  
参数里的sx 和 sy是 x轴和y轴方向的错切系数  
[skew参考文章](http://www.jianshu.com/p/11e062284491 "skew 参考")


## 2.2 使用Matrix来做变换
[深入理解Android中的Matrix-参考文章](http://www.jianshu.com/p/6aa6080373ab)  
[Custom view Matrix 介绍](http://www.gcssloop.com/page/2/#blog)  
[什么是齐次](https://www.zhihu.com/question/19816504)    
[对矩阵前乘后乘的一些认识](http://blog.csdn.net/linmiansheng/article/details/18820599)

**使用setMatrix(Matrix matrix)方法时需要关闭硬件加速**。。**否则不起作用**


### 2.2.0 Matrix说明
Matrix是Android SDK 提供的一个矩阵类（矩阵就是一个矩形阵列），代表一个3x3的矩阵.主要功能是坐标映射以及数值转换  

具体数值大概是下面图这个关系：

  
![Matrix](http://ww1.sinaimg.cn/large/6ab93b35gy1fibgs6qkpxj20ah01v0sj.jpg)  


> 其实2x2的矩阵 就可以满足运算，但是为什么android要使用3X3的呢？  
> 
> 因为以矩阵表达式来计算这些变换时，平移是矩阵相加，旋转和缩放则是矩阵相乘。那些数学大神们为了方便计算，所以引入了一样神器叫做齐次坐标，将平移的加法合并用乘法表示  
>
>**在数学中我们的点和向量都是这样表示的(x, y)，两者看起来一样，计算机无法区分，为此让计算机也可以区分它们，增加了一个标志位，增加之后看起来是这样:**
>
>(x, y, 1) - 点
>(x, y, 0) - 向量
> 
>另外，齐次坐标具有等比的性质，(2,3,1)、(4,6,2)…(2N,3N,N)表示的均是(2,3)这一个点。
> 
>对每一个齐次坐标，我们只要把它除以三元组中的第三个数，即可得到原始的二维点坐标
>
>事实上(x,y,0)就是无穷远处的点。


#### 2.2.0.1 Matrix 基本原理
基本变换有4种: 平移(translate)、缩放(scale)、旋转(rotate) 和 错切(skew)。

具体哪些数值是控制哪些功能：  
  
![](http://ww1.sinaimg.cn/large/6ab93b35gy1fibgvnbgsaj20c008zdgy.jpg)  

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fibgwggdevj20c008zdge.jpg)  

控制透视的最后三个参数 通常为(0,0,1)

##### 2.2.0.1.1 缩放
用坐标系表示:  

>x = k1x0  
>y = k2y0

用矩阵表示:   

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fibhcp97jsj205x01va9t.jpg) 


##### 2.2.0.1.2 错切

错切存在俩种：水平错切(平行x轴)和垂直错切(平行y轴)  

**水平错切:**  
>x = x0+ky0  
>y = y0

矩阵表示：  


![](http://ww1.sinaimg.cn/large/6ab93b35gy1fibhfsti9ij205j01v3y9.jpg)  

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fibhix3dl3j208c0dw3yz.jpg)  


**垂直错切:** 
>x = x0  
>y =kx0+y0

矩阵表示:  

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fibhkytz81j205j01v741.jpg)

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fibhl7x1cfj208c0dwq3f.jpg)


**复合错切:**
>x = x0 + k1y0  
>y = k2x0 + y0

矩阵表示:  

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fibhrh1az5j205x01v741.jpg)

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fibhrq38ruj208c0dwdgi.jpg)


##### 2.2.0.1.3 旋转

假定一个点 A(x0, y0) ,距离原点距离为 r, 与水平轴夹角为 α 度, 绕原点旋转 θ 度, 旋转后为点 B(x, y) 如下:  


![](http://ww1.sinaimg.cn/large/6ab93b35gy1fibi5bronzj202r00b0cw.jpg)  

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fibi67tmg0j202q00h0dk.jpg)  

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fibi49ujl5j20fr00ia9t.jpg)  

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fibi6mu2f8j20fr00idfl.jpg)   


矩阵表示:  


![](http://ww1.sinaimg.cn/large/6ab93b35gy1fibi70mutmj208a01vwe9.jpg)

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fibi8w7y02j208c0dwt8s.jpg)  



##### 2.2.0.1.4 平移


![](http://ww1.sinaimg.cn/large/6ab93b35gy1fibiadb2xtj202r00f0cm.jpg)  


![](http://ww1.sinaimg.cn/large/6ab93b35gy1fibiao4mq5j202o00g0dp.jpg) 


矩阵表示:   


![juzhen](http://ww1.sinaimg.cn/large/6ab93b35gy1fibiav8ra5j206601va9t.jpg)  

![juzhen](http://ww1.sinaimg.cn/large/6ab93b35gy1fibiazwc4tj208c0dw0su.jpg)  

#### 2.2.0.2 Matrix复合原理
其实Matrix的多种复合操作都是使用矩阵乘法实现的，从原理上理解很简单，但是，使用矩阵乘法也有其弱点，后面的操作可能会影响到前面到操作，所以在构造Matrix时顺序很重要。  

我们常用的四大变换操作，每一种变换操作在Matrix均有三类,前乘(pre)，后乘(post)和设置(set)，由于**矩阵乘法不满足交换律**，所以前乘(pre)，后乘(post)和设置(set)的区别还是很大的。  

>在图形学中，矩阵M右乘A，表示的是  A * M，而矩阵 M 左乘 A，则表示的是 M * A，可以形象地理解为右乘就是从右边乘进来，左乘就是从左边乘进来。
>
>**前乘pre:  M` = M·S （前乘相当于矩阵的右乘）S右乘M**
> 
>**后乘post:  M` = S·M (后乘相当于矩阵的左乘 ) S左乘M** 
>  
>**设置set:会直接覆盖掉之前的矩阵，所以设置会导致之前的操作失效**

>pre 和 post 不能影响程序执行顺序，而程序每执行一条语句都会得出一个确定的结果，所以，它根本不能控制先后执行，属于完全扯淡型

>由于矩阵乘法满足结合律，所以不论是靠右先执行还是靠左先执行都可以，


    /**
     * Postconcats the matrix with the specified translation.
     * M' = T(dx, dy) * M
     */
    public boolean postTranslate(float dx, float dy) {
        native_postTranslate(native_instance, dx, dy);
        return true;
    }

    /**
     * Preconcats the matrix with the specified translation.
     * M' = M * T(dx, dy)
     */
    public boolean preTranslate(float dx, float dy) {
        native_preTranslate(native_instance, dx, dy);
        return true;
    }


#### 2.2.0.3 如何使用pre和post  

在构造 Matrix 时，个人建议尽量使用一种乘法，前乘或者后乘，这样操作顺序容易确定，出现问题也比较容易排查。当然，由于矩阵乘法不满足交换律，前乘和后乘的结果是不同的，使用时应结合具体情景分析使用。


**基本定理** 

- 所有的操作(旋转、平移、缩放、错切)默认都是以坐标原点为基准点的。

- 之前操作的坐标系状态会保留，并且影响到后续状态。


**假设需要围绕某一点旋转：**  
可以用这个方法 **xxxRotate(angle, pivotX, pivotY)** ,由于我们这里需要组合构造一个 Matrix，所以不直接使用这个方法。

1. 先将坐标系原点移动到指定位置，使用平移 T
2. 对坐标系进行旋转，使用旋转 S (围绕原点旋转)
3. 再将坐标系平移回原来位置，使用平移 -T  

**具体公式：**  
>M 为原始矩阵，是一个单位矩阵， M‘ 为结果矩阵， T 为平移， R为旋转  

M' = M\*T\*R\*-T = T\*R\*-T

**按照公式写出来的伪代码如下**：

	Matrix matrix = new Matrix();
	matrix.preTranslate(pivotX,pivotY);
	matrix.preRotate(angle);
	matrix.preTranslate(-pivotX, -pivotY);


**围绕某一点操作可以拓展为通用情况，即**：

	Matrix matrix = new Matrix();
	matrix.preTranslate(pivotX,pivotY);
	// 各种操作，旋转，缩放，错切等，可以执行多次。
	matrix.preTranslate(-pivotX, -pivotY);





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

	boolean setPolyToPoly (  
        float[] src,    // 原始数组 src [x,y]，存储内容为一组点
        int srcIndex,   // 原始数组开始位置
        float[] dst,    // 目标数组 dst [x,y]，存储内容为一组点
        int dstIndex,   // 目标数组开始位置
        int pointCount) // 测控点的数量 取值范围是: 0到4

> pointCount 取值  
> 0 相当于reset
> 1 相当于 translate  
> 2 可以进行缩放 旋转 平移    
> 3 缩放 旋转 平移 错切   
> 4 缩放 旋转 平移 错切 + 任何形变     
> 通常取4就好了。。因为 其他的都可以用其他api来表示啊。。  
> 测控点选取都应当是不重复的(src与dst均是如此)，如果选取了重复的点会直接导致测量失效，这也意味着，你不允许将一个方形(四个点)映射为三角形(四个点，但其中两个位置重叠)，但可以接近于三角形    




#### 2.2.2.1 Matrix.setPolyToPoly(float[] src, int srcIndex, float[] dst, int dstIndex, int pointCount) 用点对点映射的方式设置变换  

poly 就是「多」的意思。setPolyToPoly() 的作用是通过多点的映射的方式来直接设置变换。「多点映射」的意思就是把指定的点移动到给出的位置，从而发生形变。例如：(0, 0) -> (100, 100) 表示把 (0, 0) 位置的像素移动到 (100, 100) 的位置，这个是单点的映射，单点映射可以实现平移。而多点的映射，就可以让绘制内容任意地扭曲。

	Matrix matrix = new Matrix();  
	float pointsSrc = {left, top, right, top, left, bottom, right, bottom};（俩个为一组 。。）  
	float pointsDst = {left - 10, top + 50, right + 120, top - 90, left + 20, bottom + 30, right + 20, bottom + 60};

	...

	matrix.reset();  
	matrix.setPolyToPoly(pointsSrc, 0, pointsDst, 0, 4);

	canvas.save();  
	canvas.concat(matrix);  
	canvas.drawBitmap(bitmap, x, y, paint);  
	canvas.restore();  


参数里，src 和 dst 是源点集合目标点集；srcIndex 和 dstIndex 是第一个点的偏移；pointCount 是采集的点的个数（个数不能大于 4，因为大于 4 个点就无法计算变换了）。

### 2.2.3 Matrix Api 介绍

#### 2.2.3.1 mapPoints  
计算一组点 基于当前Matrix变换后的位置（参数一般是偶数，如果是奇数则会忽略最后一个数）

#### 2.2.3.2 mapRadius  
测量经过matrix形变的半径

#### 2.2.3.3 mapRect  
测量矩形经过matrix形变的位置  

#### 2.2.3.4 mapVectors  
测量向量  
与mapPoints相同，区别是mapVectors不会受到位置影响  

#### 2.2.3.5 setRectToRect  
将src矩形 内容填充到 dst矩形中，根据stf参数选择模式 
 
	boolean setRectToRect (RectF src,           // 源区域
                RectF dst,                  // 目标区域
                Matrix.ScaleToFit stf)      // 缩放适配模式

#### 2.2.3.6 rectStaysRect  
判断矩形经过变换之后是否仍为矩形  

#### 2.2.3.7 setSinCos  
不常用 去查api

#### 2.2.3.8 矩阵相关  
1. invert 求矩阵的逆矩阵，就是计算与之前相反的矩阵（之前是位移200，逆矩阵就是位移-200）
2. isAffine  
3. isIdentity ，判断矩阵是否是 单位矩阵


## 2.3 使用Camera做三维变换
手机屏幕是一个2D的屏幕，所有的3D效果都是3D在2D平台的效果，而这就是camera的作用，使用Cemera和Matrix可以在不使用OpenGl的情况下做出简单的3D效果
### 2.3.0 Camera的简介

**Camera使用的是三维坐标系**：  

>使用左手坐标系，即左手手臂方向是 X轴正向，四指弯曲方向是Y轴正方向，大拇指方向是Z轴正方向！

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fieg2vyfq7j208c05iglp.jpg)

>x轴 左正右负  y轴 上正下负  z轴 屏幕朝里为负，朝外为正  
Camera 有一个相机位置，是在屏幕朝外  


**三维投影:**
>三维投影是将三维空间中的点映射到二维平面上的方法  
>
>一般分为 正交投影 和 透视投影  
>
>Android camera 此处使用的就是透视投影,就是在原点处的 **坐标轴位置(0,0,-8),单位：英寸**


**Camera 的旋转方向**: 

>1. x轴 是 y轴正方向进 y轴负方向出   
>2. y轴 是 x轴正方向进 x轴负方向出  
>3. z轴 是 x轴正方向往 y轴正方向出 

**三维变换有三类**：  
>旋转(rotate)，平移(translate)和移动相机(setLocation)    


**Camera 不能设置轴心，永远是原点**


### 2.3.0 快照save 和 回滚restore  
保存当前状态和恢复到上一次保存的状态

- save 有俩个方法，有一个方法是重载的，可以传入一个flag，根据这个flag参数指定保存的状态  
- save 方法 是往栈里增加一层状态，restore 是恢复栈里的一层状态
- restoreToCount()  弹出指定位置以及以上所有状态，并根据指定位置之前的位置的状态进行恢复  
- getSaveCount() 获取栈中保存状态的数量，默认为1（就是说弹出了所有状态之后 仍然是1）

### 2.3.1Camera.rotate*()三维旋转
Camera.rotate*() 一共有四个方法： rotateX(deg) rotateY(deg) rotateZ(deg) rotate(x, y, z)  

Camera 和 Canvas 一样也需要保存和恢复状态才能正常绘制，不然在界面刷新之后绘制就会出现问题

	canvas.save();
	camera.save();//保存camera的状态

	camera.rotateX(30); // 旋转 Camera 的三维空间  
	camera.applyToCanvas(canvas); // 把旋转投影到 Canvas
	camera.restore();

	canvas.drawBitmap(bitmap, point1.x, point1.y, paint);  
	canvas.restore();  

因为默认原点的关系，如果我们想实现 翻转时 对图形进对称的操作。需要移动图形到原点位置！然后再进行操作！最后再移动回去~ 
>**另外这个移动需要使用canvas来进行，因为camera 移动的话 是移动的camera的坐标轴,另外它是在变换结束之后才会做投影！**   
>**移动camera 的坐标系 不能改变相机的位置..**

	canvas.save();

	camera.save(); // 保存 Camera 的状态  
	camera.rotateX(30); // 旋转 Camera 的三维空间  
	canvas.translate(centerX, centerY); // 旋转之后把投影移动回来  
	camera.applyToCanvas(canvas); // 把旋转投影到 Canvas  
	canvas.translate(-centerX, -centerY); // 旋转之前把绘制内容移动到轴心（原点）  
	camera.restore(); // 恢复 Camera 的状态

	canvas.drawBitmap(bitmap, point1.x, point1.y, paint);  
	canvas.restore(); 


另外一种写法:  

	camera.save();
    matrix.reset();
    camera.rotateX(degree);
    camera.getMatrix(matrix);
    camera.restore();
    matrix.preTranslate(-centerX, -centerY);//右乘  把绘制内容移回来
    matrix.postTranslate(centerX, centerY);//左乘  把绘制内容移动到轴心
    canvas.save();
    canvas.concat(matrix);
    canvas.drawBitmap(bitmap, point.x, point.y, paint);
    canvas.restore();

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fies9k0ez3j20fe0ajmzs.jpg)

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fiesa6bzupj20eh0a0mz6.jpg)

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fifme9iiloj20gh0b3myg.jpg)


![](http://ww1.sinaimg.cn/large/6ab93b35gy1fifmejg5jcj20fw0b7405.jpg)

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fifmeoovbbj20gr0a4ac3.jpg)

![](http://ww1.sinaimg.cn/large/6ab93b35gy1fifmet3n57j20ft0akwg3.jpg)



### 2.3.2 Camera.translate(float x, float y, float z) 
- 沿X轴移动,以下俩种方式都能让坐标系向右移动x个单位
	
	1. camera.translate(x,0,0)  ;
	
	2. Matrix matrix = new Matrix();  
	camera.getMatrix(matrix);  
	matrix.postTranslate(x,0)

- 沿y轴移动，由于俩者y轴相反！所以采用如下方式  。另外`camera.translate(0,-y,0)`与`matrix.postTranslate(0,y)`.平移方向一致，距离相同。这俩种方式都可以让坐标系向下移动y个单位

	1. Camera camera = new Camera();  
		camera.translate(0, 100, 0);    // camera - 沿y轴正方向平移100像素

	2. Matrix matrix = new Matrix();  
		camera.getMatrix(matrix);  
		matrix.postTranslate(0,100);    // matrix - 沿y轴正方向平移100像素

### 2.3.3 Camera.setLocation(x, y, z) 设置虚拟相机的位置
**单位是 英寸！！**

>这种设计源自 Android 底层的图像引擎 Skia 。在 Skia 中，Camera 的位置单位是英寸，英寸和像素的换算单位在 Skia 中被写死为了 72 像素，而 Android 中把这个换算单位照搬了过来。  
>
>在 Camera 中，相机的默认位置是 (0, 0, -8)（英寸）。8 x 72 = 576，所以它的默认位置是 (0, 0, -576)（像素）。
>
>Camera.setLocation(x, y, z) 的 x 和 y 参数一般不会改变，直接填 0 就好。


### 2.3.4 消除camera3D旋转时的 图片过大效果


![](http://ww1.sinaimg.cn/large/6ab93b35gy1fifsnq5u0lj20i90b5di8.jpg)


# 3 图层和画布
---
## 3.1 saveLayer()

	public int saveLayer(RectF bounds, Paint paint, int saveFlags)     
	public int saveLayer(float left, float top, float right, float bottom,Paint paint, int saveFlags)


- 保存指定矩形区域的canvas内容,saveFlags取值范围ALL_SAVE_FLAG、MATRIX_SAVE_FLAG、CLIP_SAVE_FLAG、HAS_ALPHA_LAYER_SAVE_FLAG、FULL_COLOR_LAYER_SAVE_FLAG、CLIP_TO_LAYER_SAVE_FLAG总共有这六个，其中ALL_SAVE_FLAG表示保存全部内容  

- saveLayer绘图流程:在调用saveLayer时，会生成一个全新的, 指定大小的,全透明的 bitmap,接下来所有的操作都会在这个bitmap上进行 。 在xferMode里面，绘制源图像时，会将之前画布上所有的内容都作为目标图像,而在saveLayer新生成的 bitmap中，只有dst图像。 在绘制完成之后，会把saveLayer所生成的bitmap盖在原来的canvas上.


## 3.2 Layer+bitmap
图层(layer)： 一个透明图层，在调用canvas.draw 系列函数时 生成的

画布(bitmap): 每一个画布都是一个bitmap,所有的图像最终都是绘制在bitmap上的。


## 3.3 save(),saveLayer(),saveLayerAlpha()
### 3.3.1 saveLayer()

	public int saveLayer(RectF bounds, Paint paint, int saveFlags)  
	public int saveLayer(float left, float top, float right, float bottom,Paint paint, int saveFlags) 


- saveLayer之后 所有的动作都只对新建画布有效
- 传入的矩形大小就是新建的画布大小  
- saveLayer时，尽量选取比较小的区域，否则新建画布时  很容易OOM

### 3.3.2 saveLayerAlpha()

	public int saveLayerAlpha(RectF bounds, int alpha, int saveFlags)  
	public int saveLayerAlpha(float left, float top, float right, float bottom,int alpha, int saveFlags)

- 多了一个alpha参数，指定新建画布的透明度

### 3.3.3 save()

	public int save()  
	public int save(int saveFlags)  
- save()不会新建画布

## 3.4 FLAG的意义  

>ALL_SAVE_FLAG	保存所有的标识	save()、saveLayer()
>
>MATRIX_SAVE_FLAG	仅保存canvas的matrix数组	save()、saveLayer()
>
>CLIP_SAVE_FLAG	仅保存canvas的当前大小	save()、saveLayer()
>
>HAS_ALPHA_LAYER_SAVE_FLAG	标识新建的bmp具有透明度，在与上层画布结合时，透明位置显示上图图像,与FULL_COLOR_LAYER_SAVE_FLAG冲突，若同时指定，则以HAS_ALPHA_LAYER_SAVE_FLAG为主	saveLayer()
>
>FULL_COLOR_LAYER_SAVE_FLAG	标识新建的bmp颜色完全独立，在与上层画布结合时，先清空上层画布再覆盖上去	saveLayer()
>
>CLIP_TO_LAYER_SAVE_FLAG	在保存图层前先把当前画布根据bounds裁剪，与CLIP_SAVE_FLAG冲突，若同时指定，则以CLIP_SAVE_FLAG为主	saveLayer()

### 3.4.1 MATRIX_SAVE_FLAG+save()+saveLayer()

- 指定只保存位置矩阵,也就是translate,rotate,scale,skew时改变的那个Matrix.换句话说就是在调用了save(MATRIX_SAVE_FLAG) 之后，做一些操作，然后再调用restore() 只会恢复位置矩阵。

- 当save\saveLayer调用Canvas.MATRIX_SAVE_FLAG标识时只会保存画布的位置矩阵信息，在canvas.restore()时也只会恢复位置信息，而改变过的画布大小是不会被恢复的。 
- 当使用canvas.saveLayer(Canvas.MATRIX_SAVE_FLAG)时，需要与Canvas.HAS_ALPHA_LAYER_SAVE_FLAG一起使用，不然新建画布所在区域原来的图像将被清空.


### 3.4.2 CLIIP_SAVE_FLAG+saveLayer()+save()
- 意思是仅保存Canvas的裁剪信息

- 当save/saveLayer调用 Canvas.CLIP_SAVE_FLAG时只会保存画布的裁剪信息，在canvas.restore()时也只会恢复裁剪信息，而改变过的画布位置信息是不会被恢复的。 
- 当使用canvas.saveLayer(Canvas.CLIP_SAVE_FLAG)时，需要与Canvas.HAS_ALPHA_LAYER_SAVE_FLAG一起使用，不然新建画布所在区域原来的图像将被清空。


### 3.4.3 HAS_ALPHA_LAYER_SAVE_FLAG & FULL_COLOR_LAYER_SAVE_FLAG+saveLayer()

- saveLayer专用  
 
- HAS_ALPHA_LAYER_SAVE_FLAG表示新建的bitmap画布在与上一个画布合成时，不会将上一层画布内容清空，直接盖在上一个画布内容上面。 
 
- FULL_COLOR_LAYER_SAVE_FLAG则表示新建的bimap画布在与上一个画布合成时，先将上一层画布对应区域清空，然后再盖在上面。 
 
- 当HAS_ALPHA_LAYER_SAVE_FLAG与FULL_COLOR_LAYER_SAVE_FLAG两个标识同时指定时，以HAS_ALPHA_LAYER_SAVE_FLAG为主 
 
- 当即没有指定HAS_ALPHA_LAYER_SAVE_FLAG也没有指定FULL_COLOR_LAYER_SAVE_FLAG时，系统默认使用FULL_COLOR_LAYER_SAVE_FLAG；


### 3.4.4 CLIP_TO_LAYER_SAVE_FLAG +saveLayer()

- saveLayer()专用

- CLIP_TO_LAYER_SAVE_FLAG意义是在新建bitmap前，先把canvas给裁剪，一旦画板被裁剪，那么其中的各个画布都会被受到影响。而且由于它是在新建bitmap前做的裁剪，所以是无法恢复的； 

- 当CLIP_TO_LAYER_SAVE_FLAG与CLIP_SAVE_FLAG标识共用时，在调用restore()后，画布将被恢复

### 3.4.5 ALL_SAVE_FLAG save+saveLayer

- 对于save(int flag)来讲，ALL_SAVE_FLAG = MATRIX_SAVE_FLAG | CLIP_SAVE_FLAG；即保存位置信息和裁剪信息 

- 对于saveLayer(int flag)来讲，ALL_SAVE_FLAG = MATRIX_SAVE_FLAG | CLIP_SAVE_FLAG|HAS_ALPHA_LAYER_SAVE_FLAG；即保存保存位置信息和裁剪信息，新建画布在与上一层画布合成时，不清空原画布内容。

## 3.5 restore()和restoreToCount()

- restore的意义是把回退栈中的最上层画布状态出栈，恢复画布状态。restoreToCount(int count)的意义是一直退栈，直到指定层count+1做为栈顶，将此之前的所有动作都恢复。 

- 所以无论哪种save方法，哪个FLAG标识，保存画布时都使用的是同一个栈 

- restore()与restoreToCount(int count)针对的都是同一个栈，所以是完全可以通用和混用的。

# 4 关闭加速
---
自定义View中关闭加速  

        setLayerType(LAYER_TYPE_SOFTWARE, null);
清单文件中关闭硬件加速(关闭的是整个应用的硬件加速)  

	android:hardwareAccelerated = "false"