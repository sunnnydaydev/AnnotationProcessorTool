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

###### 5、API练习#ExecutableElement

```java
   //获得所有注解作用的方法元素，解析方法上的注解信息。
        for (Element element : roundEnvironment.getElementsAnnotatedWith(CheckGetter.class)) {
            ExecutableElement executableElement = (ExecutableElement) element;// 由于作用于方法所以可以直接强转方法类型
            TypeElement classElement = (TypeElement) executableElement.getEnclosingElement(); // 类型之间还可以转换，方法的外层即类
            PackageElement packageElement = elementUtil.getPackageOf(classElement);// 根据元素直接获得包类型

            String pkgName = packageElement.getQualifiedName().toString();// 包名
            String fullClassName = classElement.getQualifiedName().toString(); //全类名
            String methodName = executableElement.getSimpleName().toString();//方法名
            note("--------------process method info-----------");
            note("pkgName:" + pkgName);
            note("fullClassName:" + fullClassName);
            note("methodName:" + methodName);

           // 方法参数列表元素:一般用于获取参数类型、参数名。
            List<? extends VariableElement> methodParameters = executableElement.getParameters();
            List<String> types = new ArrayList<>();
            for (VariableElement variableElement : methodParameters) {// 遍历每个参数元素
                TypeMirror methodParameterType = variableElement.asType();

                String parameterName = variableElement.getSimpleName().toString(); //参数名
                String parameterKind = methodParameterType.toString(); //参数类型
                note("------------process method param-------------");
                note("parameterName:" + parameterName);
                note("parameterKind:" + parameterKind);
                note("typeVariable:" + methodParameterType);
            }
            note("types:" + types.toString());
        }
```

###  五、注解处理器的常见用途：代码生成

> 搞个简单的代码生成栗子

###### 1、定义注解

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface BindView {
    int value();
}
```

###### 2、处理注解生成代码

```java


/**
 * Created by sunnyday on 2020/6/10 
 * 注解处理器：JavaFileObject 代码生成练习
 */
@AutoService(Processor.class)
public class BindViewProcessorPractise extends AbstractProcessor {

    private Filer mFiler;
    private Messager mMessager;
    private Elements mElementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mMessager = processingEnvironment.getMessager();
        mElementUtils = processingEnvironment.getElementUtils();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(BindView.class.getCanonicalName());
        return set;
    }


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> bindViewElements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        for (Element element : bindViewElements) {
            //1.获取包名
            PackageElement packageElement = mElementUtils.getPackageOf(element);
            String pkName = packageElement.getQualifiedName().toString();
            note(String.format("package = %s", pkName));

            //2.获取包装类类型
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            String enclosingName = enclosingElement.getQualifiedName().toString();
            note(String.format("enclosindClass = %s", enclosingElement));


            //因为BindView只作用于filed，所以这里可直接进行强转
            VariableElement bindViewElement = (VariableElement) element;
            //3.获取注解的成员变量名
            String bindViewFiledName = bindViewElement.getSimpleName().toString();
            //3.获取注解的成员变量类型
            String bindViewFiledClassType = bindViewElement.asType().toString();



            //4.获取注解元数据
            BindView bindView = element.getAnnotation(BindView.class);
            int id = bindView.value();
            note(String.format("%s %s = %d", bindViewFiledClassType, bindViewFiledName, id));

            //4.生成文件
            createFile(enclosingElement, bindViewFiledClassType, bindViewFiledName, id);
            return true;
        }
        return false;
    }

    private void createFile(TypeElement enclosingElement, String bindViewFiledClassType, String bindViewFiledName, int id) {
        String pkName = mElementUtils.getPackageOf(enclosingElement).getQualifiedName().toString();
        try {
            // 参数：文件全类名，元素
            JavaFileObject jfo = mFiler.createSourceFile(pkName + ".ViewBinding", new Element[]{});
            Writer writer = jfo.openWriter();
            writer.write(brewCode(pkName, bindViewFiledClassType, bindViewFiledName, id));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String brewCode(String pkName, String bindViewFiledClassType, String bindViewFiledName, int id) {
        StringBuilder builder = new StringBuilder();
        builder.append("package " + pkName + ";\n\n");
        builder.append("/**\n" +
                " * Created by zb on 2020/6/12 \n" +
                " * Auto generated by APT,do not modify here!!\n"+
                " */\n\n");

        builder.append("public class ViewBinding { \n\n");
        builder.append("public static void main(String[] args){ \n");
        String info = String.format("%s %s = %d", bindViewFiledClassType, bindViewFiledName, id);
        builder.append("System.out.println(\"" + info + "\");\n");
        builder.append("}\n");
        builder.append("}");
        return builder.toString();
    }
    private void note(String msg) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, msg);
    }

    private void note(String format, Object... args) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, String.format(format, args));
    }

}

```

> 整体思路：
>
> 1、定义注解
>
> 2、注解处理器处理注解，获得注解作用字段相关信息
>
> 3、使用JavaFileObject、Writer、Filer来生成新的代码。

### 六、ButterKnife的BindView实现原理

> 上述我们使用java提供的JavaFileObject、Writer、Filer来生成新的代码。其实有个库javapoet 可以帮我们快速生成代码。

###### 1、JavaPoet 常见类

> - MethodSpec：代表一个构造方法或方法声明；
> - TypeSpec：代表一个类、接口、或者枚举声明；
> - FieldSpec：代表一个成员变量、字段声明；
> - JavaFile：包含一个顶级类的 Java 文件；
>
> [详情参考官网](https://github.com/square/javapoet)



###### 2、ButterKnife 的实现

>定义注解-->注解处理器处理注解-->生成 Java 文件-->引入代码

（1）定义注解

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface BindView {
    int value();
}
```

（2）处理注解&生成java文件

```java

/**
 * Created by sunnyday on 2020/6/12 
 */
@AutoService(Processor.class)
public class BindViewProcessor extends AbstractProcessor {

    private Elements mElementUtil;
    private Messager mMessager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mElementUtil = processingEnvironment.getElementUtils();
        mMessager = processingEnvironment.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> typeSet = new HashSet<>();
        typeSet.add(BindView.class.getCanonicalName());
        return typeSet;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        collectBindViewAnnotations(roundEnvironment);
        return false;
    }

    /**
     * deal with element
     *
     * @param roundEnvironment roundEnvironment
     */
    private boolean collectBindViewAnnotations(RoundEnvironment roundEnvironment) {
        Set<? extends Element> elementSet = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        if (null == elementSet || elementSet.isEmpty()) {
            tips(Diagnostic.Kind.NOTE, " no @BindView annotation find in your code.");
            return false;
        }
        Map<TypeElement, Map<Integer, VariableElement>> typeElementMapMap = new HashMap<>();
        // 1、遍历所有的注解元素，把元素的信息放入typeElementMapMap
        for (Element element : elementSet) {
            if (element.getKind() != ElementKind.FIELD) {
                tips(Diagnostic.Kind.ERROR, "only field of class can use @%s annotation.", BindView.class.getSimpleName());
                return false;
            }
            VariableElement variableElement = (VariableElement) element;//获得作用的成员字段
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();// 获得字段所处的类

            //取typeElementMapMap中的value（variableElementMap）,为null时填充数据。
            Map<Integer, VariableElement> variableElementMap = typeElementMapMap.get(typeElement);
            if (variableElementMap == null) {
                variableElementMap = new HashMap<>();
                typeElementMapMap.put(typeElement, variableElementMap);
            }
            // 获得注解元素id值,添加到variableElementMap
            int viewId = variableElement.getAnnotation(BindView.class).value();
            variableElementMap.put(viewId, variableElement);
        }
        //2、遍历typeElementMapMap 进行信息处理
        for (TypeElement typeElement : typeElementMapMap.keySet()) {
            //根据key取typeElementMapMap得value
            Map<Integer, VariableElement> variableElementMap = typeElementMapMap.get(typeElement);
            String pkgName = ElementUtil.getPkgName(mElementUtil, typeElement);
            JavaFile javaFile = JavaFile.builder(pkgName, generateClassByPoet(typeElement, variableElementMap)).build(); // 获得个java文件
            try {
                javaFile.writeTo((File) processingEnv.getFiler());//  写文件
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return true;
    }

    /**
     * 生成java类
     *
     * @param typeElement        这里可以认为元素为 activity（上文字段的getEnclosingElement获得的）
     * @param variableElementMap 注解作用的字段值和字段元素封成的集合
     */
    private TypeSpec generateClassByPoet(TypeElement typeElement, Map<Integer, VariableElement> variableElementMap) {

        return TypeSpec
                .classBuilder(ElementUtil.getEnclosingClassName(typeElement) + "ViewBinding")//生成类名：XxxActivity+ViewBinding
                .addModifiers(Modifier.PUBLIC)
                .addMethod(generateMethodByPoet(typeElement, variableElementMap)) //为类添加方法
                .build();
    }

    private MethodSpec generateMethodByPoet(TypeElement typeElement, Map<Integer, VariableElement> variableElementMap) {
        ClassName className = ClassName.bestGuess(typeElement.getQualifiedName().toString());
        String parameter = "_" + StringUtil.toLowerCaseFirstChar(className.simpleName());
        MethodSpec.Builder builder = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(className, parameter);
        for (int viewId : variableElementMap.keySet()) {
            VariableElement variableElement = variableElementMap.get(viewId);
            // 被注解的字段名
            String filedName = variableElement.getSimpleName().toString();
            // 被注解的字段类型
            String filedType = variableElement.asType().toString();
            String text = "{0}.{1}=({2})(({0}).findViewById({3}));\n";// 取后面的站位符号
            builder.addCode(MessageFormat.format(text, parameter, filedName, filedType, String.valueOf(viewId)));
        }

        return builder.build();
    }

    /**
     * red msg log, watch in "AS-->Build--> Build OutPut"
     */
    private void tips(Diagnostic.Kind kind, String msg, Object... args) {
        mMessager.printMessage(kind, String.format(msg, args));
    }
}

```

（3）引入

> 定义工具类，MainActivity onCreate 进行绑定。其实也就是反射调用生成的代码。

```java
public class ButterKnife {
    public static void bind(Activity activity) {
        Class clazz = activity.getClass();
        try {
            Class bindViewClass = Class.forName(clazz.getName() + "ViewBinding");
            Method method = bindViewClass.getMethod("bind", clazz);
            method.invoke(null, activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```



[项目中可能会碰到的坑](https://github.com/sunnnydaydev/AnnotationProcessorTool/blob/master/bug.txt)

参考：

1、https://doc.mcust.cn/jvm/article/40189.html#%E6%B3%A8%E8%A7%A3%E5%A4%84%E7%90%86%E5%99%A8%E7%9A%84%E5%8E%9F%E7%90%86

2、https://www.jianshu.com/p/bcddc376c0ef

3、https://www.jianshu.com/p/d7567258ae85

4、https://github.com/Omooo/Android-Notes/blob/master/blogs/Android/APT.md


