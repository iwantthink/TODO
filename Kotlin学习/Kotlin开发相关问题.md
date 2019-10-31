# 1. 多模块问题

每个模块都需要添加插件`kotlin-android`,否则模块之间的引用可能报错！！！！
	
	apply plugin: 'kotlin-android'

例如对模块间常量的引用可能会报错，模块A的注解传入模块B的常量，将导致编译失败！！！！


# 2. Dagger 

[Dagger官方文档](https://dagger.dev/android)

[Dagger 技术文档](https://juejin.im/post/5c5db9bb518825629c5680c1)

## 2.0 Dagger2注入问题!

提供依赖有俩种方式

1. 使用`@Inject`注解修饰构造函数

	如果需要依赖注入的变量拥有父类，并且父类中有被`@Inject`修饰的变量(其会从Module或Inject寻找)

2. 使用`@Module`和`@Provides`提供实例

	如果需要依赖注入的变量拥有父类，并且父类中有被`@Inject`修饰的变量(其不会寻找)


举例：

	class Student @Inject constructor() : Human()

	open class Human {
	
	    @Inject
	    lateinit var name: String
	}
	
	@Component(modules = [StudentModule::class])
	interface StudentComponent {
	
	    fun inject(activity: MainActivity)
	}

	@Module
	class StudentModule {
	
	//    @Provides
	//    fun providesStudent(): Student {
	//        return Student()
	//    }
	
	    @Provides
	    fun providesName(): String {
	        return "ryan!!!!!"
	    }
	}
	
	// 具体使用
	class MainActivity : AppCompatActivity() {
	
	    @Inject
	    lateinit var stu: Student
	    
	    override fun onCreate(savedInstanceState: Bundle?) {
	        super.onCreate(savedInstanceState)
	        setContentView(R.layout.activity_main)
	
	        DaggerStudentComponent.builder()
	            .build()
	            .inject(this)
	        
	        assert(stu.name!=null)
            
	     }
	}
	
- 👆的代码可以正常运行,dagger2生成的代码如下(篇幅有限只展示最重要的部分)

		  // MainActivity调用inject()方法后的内容
		  private MainActivity injectMainActivity(MainActivity instance) {
		  // 先生成Student,完成Student内部需要注入的变量,再注入到MainActivity成员变量中
		  MainActivity_MembersInjector.injectStu(instance, getStudent());
		   
		    return instance;
		  }

		  // 该方法会生成一个Student对象，并使用Dagger2根据Human类生成的Human_MembersInjector对该对象的name字段进行注入
		  private Student getStudent() {
		    return injectStudent(Student_Factory.newStudent());
		  }
		  
- 如果👆的代码进行修改, Student类实例改为Module提供,那么onCreate()中的代码会报错(因为Dagger生成的代码中，不会对Student的父类中需要注入的变量进行注入)


## 2.1  Dagger是如何查找所需的依赖实例进行注入了?


步骤如下：

1. 查找Component的Module中是否存在创建该**类型**的方法（前提是`@Conponent`标记的接口中包含了`@Module`标记的Module类，如果没有则直接找`@Inject`对应的构造方法）

	- 若存在方法，查看该方法是否有参数

		1. 若不存在参数，直接初始化该类的实例，一次依赖注入到此结束。

		2. 若存在参数，则从步骤1开始寻找方法去初始化每个参数

2. 若`Module`中不存在创建类方法，则查找该**类型**的类中有`@Inject`标记的构造函数，并查看构造函数中是否需要参数

	- 若不存在被`@Inject`标记的构造函数,实际上在编译期间就已经报错了

	- 若存在被`@Inject`标记的构造函数

		1. 若构造方法中无参数，则直接初始化该类实例，一次依赖注入到此结束。
	
		2. 若构造方法中有参数，从步骤1依次开始初始化每个参数。



如果既没有`@Module`提供的实例，也没有`@Inject`标记的构造方法会怎样？很简单，编译期就会报错

## 2.2 Named注解无效？？？

[Dagger2上对于Named注解无效时的解决办法!!](https://github.com/google/dagger/issues/848)

当使用`@Named`注解想解决依赖迷失的问题时...由于使用的是`Kotlin`开发，需要在注解前添加`@field:`,否则无效！！！！

- 依赖迷失即Module中提供超过一个方法返回同一种类型，这时Dagger无法知道需要使用哪个方法


## 2.3 Scope注解???
**`@Scope`注解实际上只能保证在同一个Component内的某个实例是单例的！！**

像Dagger2提供的`@Singleton`或者使用`@Scope`自定义的注解，实际上的作用只是提供字面信息(例如我自定义一个`@ActivityScope`,它只能保证在指定的Component中的实例是单例,如果存在多个Component，那不同的Component返回的实例也是不同的)

- 因此如果想要保证某个实例时全局的单例，那么必须保证对应的Component是单例的！！

- [参考文章](https://juejin.im/post/5ba4b5dbf265da0abb143401)


## 2.4 Component可以用来提供实例

如果Component的方法返回值被设置了类，那么生成的代码中就会返回该类的实例!

例如下面的代码,生成的代码中，会提供Bird实例！！！

	class Bird @Inject constructor()

	@Component
	interface BirdComponent {
	    fun getBird(): Bird
	}

Component需要和@Inject被注入的变量配合使用！

- 实际上需要通过Component方法中的参数进行关联!

如果仅仅是设置了Component,没有在对应的Activity/Fragment的变量中设置`@Inject`,那么生成的代码并没有用！

	@Component
	interface BirdComponent {
	    fun inject(activity: SeconedActivity)
	}

	public final class DaggerBirdComponent implements BirdComponent {
	  private DaggerBirdComponent(Builder builder) {}
	
	  public static Builder builder() {
	    return new Builder();
	  }
	
	  public static BirdComponent create() {
	    return new Builder().build();
	  }
	
	  @Override
	  public void inject(SeconedActivity activity) {}
	
	  public static final class Builder {
	    private Builder() {}
	
	    public BirdComponent build() {
	      return new DaggerBirdComponent(this);
	    }
	  }
	}

## 2.6 依赖注入可空的成员变量？？？

`@Inject`进行依赖注入时，并且需要延迟注入,这时可以使用`lateinit`进行

    @Inject
    lateinit var mPresenter: P

这时，如果该变量可能为空,那么就不能使用`lateInit`修饰符,然后被注入的字段不能是private的

	// 错误,lateinit无法用在可空字段
    @Inject
    lateinit var mPresenter: P? = null
	
	// 错误,此时的字段是private，而Inject无法在私有字段上生效
    @Inject
    var mPresenter: P? = null

	// 正确！！
    @Inject
    @JvmField
    var mPresenter: P? = null

[参考文章](https://codeday.me/bug/20190205/608381.html)

## 2.7 Dagger Android 支持库的使用！！！！
TODO
[Dagger Android支持库的使用](https://juejin.im/post/5c95b874e51d4502b70c157d)

# 3. RxJava的生命周期泄露问题?

RxJava在Android中通常在Activity或者Fragment中使用，这就可能导致Activity/Fragment 会在RxJava异步执行完之后已经被关闭，那么久会导致内存泄露

解决办法：

1. 基础办法：使用Disposable 在onDestroy()中手动结束

2. 三方框架，例如trello.rxlifecycle ....

TODO: 现在项目中使用三方框架，感觉不适合，需要移除掉!!

[参考文章](https://juejin.im/post/5b0cdcac518825155e4d655f)

[MVP生命周期泄露问题!](https://blog.csdn.net/qq137722697/article/details/78275882)


# 4. MVP

可以定义一个Contract类，用来保存MVP中的View接口和Present接口，便于查找和维护！

## 4.1 MVP模式

View层中，初始化ListView，这时需要用到数据，这个数据是保存在View层中还是P层中???



# 5. Retrofit

## 5.1 Retrofit+RxJava

[参考文章](https://blog.csdn.net/carson_ho/article/details/79125101)


# 6. EditText
目前不知道怎么为失去焦点的但是有内容的EditText单独设置下划线的颜色!!!!

# 7. ToolBar

## 7.1 标题居中

默认对Toolbar设置的标题是不能居中的，必须得靠自定义来实现

- 即往toolBar标签中加入textView标签，再对这个标签的位置进行设置


# 8.Fragment
TODO
关于Fragment的懒加载等操作!!!!!!

- 最新版本的androidx 提供了 setMaxLifecycle 等功能，有新的懒加载实现的形式!!!

## 8.1 AppFragment中为什么showNoContentView方法中的mNoContentView即使页面重新创建了仍然非空!!

[ViewStub相关文档](https://cloud.tencent.com/developer/article/1476470)

## 8.2 FragmentStatePagerAdapter

FragmentStatePagerAdapter 每次都会调用Adapter的getItem(), 因此如果持有Fragment的引用会导致内存泄露.......

- 解决办法就是:使用FragmentStatePagerAdapter时，在对应的Adapter中，实时创建新的Fragment实例，而不是创建好了再赋值！！！！

- 这里实际上可以分析一下为什么会出现内存泄露


FragmentPagerAdapter 不会每次都会调用Adapter的getItem(), 它会复用FragmentSupportManager中通过getItem()加载的Fragment实例


# 9. 布局页面

`android:tint="color"`属性是什么作用??

- 貌似可以改变图片颜色


include标签不要直接设置visibility，最好对内部的内容设置visibility。。否则就得去操作include标签的visibility了！！！！！

- 血的教训，一直对内部控件进行设置visibility，但是include标签的visibility并没有被改变，所以无法可见


## 9.1 MpAndroidChart

[中文博客-图例](https://blog.csdn.net/Honiler/article/details/80074019)

[文档翻译](https://juejin.im/post/5c7647cff265da2d98091035)


## 9.2 ConstraintLayout

ConstraintLayout+RecyclerView 

其中itemview 也是constraintLayout布局,然后再设置点击事件改变了itemview中某个控件的可见度之后，itemview布局变成了wrap_content!!!!!!!


CustomIndicatorsDialog!!!!!

## 9.3 AppBarLayout

在特定的Android Studio 中，会报出Render Problem
	
[Github-issue](https://github.com/material-components/material-components-android)

[Issue Tracker](https://issuetracker.google.com/issues/132316448)