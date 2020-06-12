# java AnnotationProcessorTool（注解处理器）

### ![](https://github.com/sunnnydaydev/AnnotationProcessorTool/blob/master/photos/APT.png)

### 一、概述

###### 1、[注解回顾](https://github.com/sunnnydaydev/AnnotationProcessorTool/blob/master/%E6%B3%A8%E8%A7%A3%E7%AE%80%E4%BB%8B.md)



###### 2、注解的处理方式

> 注解只是一种标记信息，所以需要我们自己去处理注解，处理注解的方式有两种：
>
> 1、编译时注解处理：编译时注解就需要使用注解处理器（Annotation Processor）进行处理。
>
> 2、运行时注解处理：运行时注解，我们可以通过反射获取注解信息，进而进行相应处理。
>
> 

###### 3、注解处理器简介

> 简称APT，AnnotationProcessorTool的缩写。我们知道，Java的注解机制允许开发人员自定义注解。这些自定义注解同样可以为Java编译器添加编译规则。不过，这种功能需要由开发人员提供，并且以插件的形式接入Java编译器中，这些插件我们称之为注解处理器（annotation processor）。

### 三、注解处理器的原理

###### 1、Java编译器的工作流程

> 在介绍注解处理器之前，我们先来了解一下Java编译器的工作流程。如下图所示 ，Java源代码的编译过程可分为三个步骤：
>
> 1. 将源文件解析为抽象语法树；
> 2. 调用已注册的注解处理器；
> 3. 生成字节码。
>
> 如果在第2步调用注解处理器过程中生成了新的源文件，那么编译器将重复第1、2步，解析并且处理新生成的源文件。每次重复我们称之为一轮（Round）。也就是说，第一轮解析、处理的是输入至编译器中的已有源文件。如果注解处理器生成了新的源文件，则开始第二轮、第三轮，解析并且处理这些新生成的源文件。当注解处理器不再生成新的源文件，编译进入最后一轮，并最终进入生成字节码的第3步。

![](https://github.com/sunnnydaydev/AnnotationProcessorTool/blob/master/photos/1.png)

###### 2、注解处理器的用途

> 1、一是定义编译规则，并检查被编译的源文件（如java自带的@Override）
>
> 2、二是修改已有的源代码（这种方式很少使用，涉及了 Java 编译器的内部 API，可能会存在兼容性问题）
>
> 3、三是生成新的源代码（比较常见，目前最常用的方式。如Butterknife、EventBus 等框架，）

###### 3、Processor 接口

```java
public interface Processor {

  void init(ProcessingEnvironment processingEnv);
  
  Set<String> getSupportedAnnotationTypes();
  
  SourceVersion getSupportedSourceVersion();
  
  boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv);
  
  ...
}
```

> 1、所有的注解处理器类都需要实现接口`Processor`
>
> 2、该接口主要有四个重要方法。其中，`init`方法用来存放注解处理器的初始化代码。之所以不用构造器，是因为在Java编译器中，注解处理器的实例是通过反射API生成的。也正是因为使用反射API，每个注解处理器类都需要定义一个无参数构造器。
>
> 3、通常来说，当编写注解处理器时，我们不声明任何构造器，并依赖于Java编译器，为之插入一个无参数构造器。而具体的初始化代码，则放入`init`方法之中。

（1）init方法

> 在这个方法中我们一般调用processingEnv.getXXX 来获得一些工具类如下代码:
>
> - Elements：用于处理程序元素的工具类
> - Types：用于处理类型数据的工具类；
> - Filter：用于给注解处理器创建文件；
> - Messager：用于给注解处理器报告错误、警告、提示等信息。

```java
    private Messager messager;
    private Elements elementUtil;
    private Filer filer;
    private Types types;
   /**
     * 存放注解处理器的初始化代码
     *
     * @param processingEnvironment 通过参数可以获得一系列的工具类
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        elementUtil = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        types = processingEnvironment.getTypeUtils();
        super.init(processingEnvironment);
    }
```

（2）getSupportedAnnotationTypes

> 方法返回注解处理器所支持的注解类型，这些注解类型只需用字符串形式表示即可。一般为固定写法如下：

```java
   /**
     * 返回注解处理器所支持的注解类型。
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new LinkedHashSet<>();
        set.add(CheckGetter.class.getCanonicalName());
        return set;
    }
```

（3）getSupportedSourceVersion

> 方法返回该处理器所支持的Java版本，通常，这个版本需要与你的Java编译器版本保持一致。一般也为固定写法。如下：

```java
   /**
     * 返回注解处理器支持的java版本，通常返回你自己的java版本即可。
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
```



（4）process 

> 1、`process`方法则是最为关键的注解处理方法，
>
> 2、JDK提供了一个实现`Processor`接口的抽象类`AbstractProcessor`。该抽象类实现了`init`、`getSupportedAnnotationTypes`和`getSupportedSourceVersion`方法。
>
> 3、它的子类可以通过`@SupportedAnnotationTypes`和`@SupportedSourceVersion`注解来声明所支持的注解类型以及Java版本。（这条我们可以忽略，按照上文固定写法即可）
>
> 4、开发者自定义注解处理器时继承AbstractProcessor即可。在编译时编译器会检查 AbstractProcessor 的子类，并且自动调用其 process() 方法。

### 四、核心API

> 注解处理器的难度在于api较多、配置容易出错，吧这些搞明白了，就ojbk了。

###### 1、process 方法

```java
 @Override
public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
  // todo 处理注解作用的元素
}
```

> 两个参数：
>
> 1、set：分别代表该注解处理器所能处理的注解类型
>
> 2、roundEnvironment：囊括当前轮生成的抽象语法树

（1）RoundEnvironment 我们一般这样玩

```java
 Set<? extends Element> elementSet = roundEnvironment.getElementsAnnotatedWith(BindView.class);//BindView 为自定义的注解
 for (Element element : elementSet) {
  // todo 处理元素
}

```

> Element是一个接口，代表元素，他的实现类有很多如下，代表特定的元素：
>
> 1、PackageElement：表示一个包程序元素
>
> 2、TypeElement：表示一个类或接口程序元素
>
> 3、ExecutableElement：表示某个类或接口的方法、构造方法或初始化程序，包括注释类型元素
>
> 4、VariableElement：表示一个字段、enum 常量、方法或构造方法参数、局部变量或异常参数
>
> 5、TypeParameterElement：表示类、接口、方法元素的类型参数

###### 2、Element子类代表元素栗子

```java
package foo;     // PackageElement

class Foo {      // TypeElement
  int a;           // VariableElement
  static int b;    // VariableElement
  Foo () {}        // ExecutableElement
  void setA (      // ExecutableElement
    int newA         // VariableElement
  ) {}
}
```

> 这下明了了吧，注解作用于上述的哪个字段时，我们真正获得的就是那种类型。例如我们常见的Butterknife 的BindView作用于成员变量的字段上，则roundEnvironment.getElementsAnnotatedWith(BindView.class)返回的集合都是VariableElement类型。

###### 3、API练习#TypeElement

> 假如我们定义个注解作用于类接口上，则我们可以获得如下信息：

```java
        for (Element element : roundEnvironment.getElementsAnnotatedWith(CheckGetter.class)) {
            TypeElement clazzElement = (TypeElement) element; // TypeElement 代表类或者接口元素，可以获得类或者接口的信息。

         //   PackageElement packageElement = (PackageElement) element.getEnclosingElement();// PackageElement 代表包元素

            String fullClassName = clazzElement.getQualifiedName().toString();   //全类名
            String className = clazzElement.getSimpleName().toString();   // 类名
            String pkgName = clazzElement.getEnclosingElement().toString();// 包名
            String parentName = clazzElement.getSuperclass().toString();// 父类名

            note("--------------------------------process class info ----------------------------");
            note("fullClassName:" + fullClassName);
            note("className:" + className);
            note("pkgName:" + pkgName);
            note("parentName:" + parentName);

        }

 /**
     * @param msg 要打印的信息
     * @function Build->Build Output 窗口查看打印消息
     */
    private void note(String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }
```



###### 4、API练习#VariableElement

> 假如我们定义个注解作用于字段上，我们可进行如下练习：

```java
  //解析属性上的注解，获得每个属性上的元素信息。
        for (Element element : roundEnvironment.getElementsAnnotatedWith(CheckGetter.class)) {
            VariableElement variableElement = (VariableElement) element;
            TypeMirror typeMirror = variableElement.asType();

            CheckGetter checkGetter = element.getAnnotation(CheckGetter.class);
           // checkGetter.value() 注解定义值时这里可以取出

            note("--------------------------------process field info ----------------------------");
            note("typeMirror:" + typeMirror.toString()); // 变量类型全名（带包名）
            note("variableElement.getSimpleName:" + variableElement.getSimpleName()); // 变量名
            note("variableElement.getEnclosingElement:" + variableElement.getEnclosingElement());


        }
```





待续！！！



