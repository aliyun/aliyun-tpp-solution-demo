# aliyun-tpp-solution-demo 推荐方案的demo

包含多个demo，可以直接修改使用，上线前请充分测试，如有雷同实属巧合。

## 环境
* 代码仓库codeup
* jdk使用 1.8
* maven 3.3+
* [maven settings.xml](#本地使用的settings.xml) 

## 目录结构
* src
  * main 
    * java 包含多个demo，可以直接修改使用
      * ABFS使用demo
      * BE使用demo
      * HTTP使用demo
      * Predict使用demo
      * Redis使用demo
      * hello最简单的demo
      * demo完整的推荐demo
        * 入口：DemoSolution
        * match：BE和redis(x2i)
        * feature:user-abfs,context-noting,item-redis
        * rank：pai-eas(multi_tower)
        * rerank：倒序+过滤+打散
  * test 测试用例
    非常推荐写测试用例，先在本地测试用例通过后，再发布到tpp
* pom.xml不要修改，加了新的也不会生效

## 关于dependency
总所周知，java依赖包纷繁复杂，一不小心就会出现冲突、漏洞、安全隐患。所以tpp的方案代码不支持用户随便使用jar包，我们会严格控制，尽量保障代码安全可靠。
控制方案：
* 1、采用定制sdk
* 2、采用定制插件
所以pom.xml里写出来的依赖就是可以用的，加了新的dependency最终也不会生效

### 基础的SDK solution-protocol
提供必要的jar
<dependency>
<groupId>com.aliyun.tpp</groupId>
<artifactId>solution-protocol</artifactId>
<version>1.0</version>
</dependency>
比如：context、result、solution

### 常用的服务 ai-service-sdk
提供一些常用的jar，这类jar不是所有人都需要，但比较常用
<dependency>
<groupId>com.aliyun.tpp</groupId>
<artifactId>ai-service-sdk</artifactId>
<version>1.0</version>
</dependency>
比如：abfs、be、eas、redis之类的

### 本地测试 ai-service-sdk-testing
提供一些测试必须的jar
<dependency>
<groupId>com.aliyun.tpp</groupId>
<artifactId>ai-service-sdk-testing</artifactId>
<version>1.0</version>
<scope>test</scope>
</dependency>

## 常见问题
### 本地使用的settings.xml
修改3个地方mirrors profiles activeProfiles
* mirrors修改
```xml

<mirrors>
  <mirror>
    <id>maven-mirror</id>
    <mirrorOf>central</mirrorOf>
    <name>maven public mirror</name>
    <url>https://repo1.maven.org/maven2/</url>
  </mirror>
</mirrors>

```
* profiles修改
```xml
<profiles>
<profile>
  <id>aliyun</id>
  <repositories>
    <repository>
      <id>aliyun</id>
      <url>https://maven.aliyun.com/repository/public</url>
      <releases>
        <enabled>true</enabled>
      </releases>
      <snapshots>
        <enabled>false</enabled>
      </snapshots>
    </repository>

  </repositories>
  <pluginRepositories>
    <pluginRepository>
      <id>aliyun</id>
      <url>https://maven.aliyun.com/repository/public</url>
      <releases>
        <enabled>true</enabled>
      </releases>
      <snapshots>
        <enabled>false</enabled>
      </snapshots>
    </pluginRepository>
  </pluginRepositories>
</profile>
</profiles>
```
* activeProfiles修改
```xml
<activeProfiles>
<activeProfile>aliyun</activeProfile>
</activeProfiles>
```
### 连通redis
* TPP调用时，将TPP实例使用的交换机网段添加到redis白名单，就能调通
* 本地测试时，将本地ip添加到redis白名单，就能调通
### 连通pai-eas
* TPP不允许调用公网，只能使用VPC地址调用
* 本地测试可以使用公网地址调用，但用完最好关闭，以免出现安全问题
