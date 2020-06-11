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

![]()





文档 待续！！！

