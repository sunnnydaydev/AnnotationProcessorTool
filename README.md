# java AnnotationProcessorTool（注解处理器）

### ![](https://github.com/sunnnydaydev/AnnotationProcessorTool/blob/master/photos/APT.png)

### 一、概述

###### 1、[注解回顾](https://github.com/sunnnydaydev/AnnotationProcessorTool/blob/master/%E6%B3%A8%E8%A7%A3%E7%AE%80%E4%BB%8B.md)



###### 2、注解处理器简介

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
> 2、二是修改已有的源代码（涉及了 Java 编译器的内部 API，可能会存在兼容性问题）
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





待续！！！

