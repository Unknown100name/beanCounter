## 简介
用于在 Spring 启动时统计系统中 Bean 的调用次数(包括方法的调用次数)

### 暂且无法实现的功能
1. 对于 final 类、abstract 类、interface 类、匿名类、注解类 没有进行统计
2. 对于 final 方法没有进行统计

### 特性
1. 支持循环依赖链排除, 但对于循环依赖链中部分 Bean 仍有可能经过 bean-counter 的包装
2. 对于 BeanPostProcessor 与 BeanFactoryPostProcessor 没有进行统计

## 支持参数与返回值

### 参数类([BeanCounterParam](src/main/java/client/domain/BeanCounterParam.java))
- maxReturnSize：返回结果集最大返回个数 (默认为全部返回)
- searchMethod：返回结果集中, 是否包含方法,否则只返回类信息(默认为 false)
- maxCount：返回结果集中, 类最大调用次数 (可选)
- minCount：返回结果集中, 类最小调用次数 (可选)
- order: 排序(可选: "count" 计数排序, "alpha" 字典排序, 不填默认排序)
- className: 类名模糊搜索 (可选)

### 结果类([BeanCounterResult](src/main/java/client/domain/BeanCounterResult.java))
- totalClass：返回结果集个数
- beanCounterParam：传递参数
- errorMessage：错误信息
- classDataList： 类统计信息
    - className：类名
    - count：被调用次数(类被调用次数 ≠ 返回的所有方法被调用次数之和)
    - methodDataList：方法统计信息
        - methodName：方法名
        - count：被调用次数

## 最佳实践

### maven 依赖
```xml
<dependency>
    <groupId>org.unknown100name</groupId>
    <artifactId>beancounter</artifactId>
    <version>1.0</version>
</dependency>
```
注意：需要自行 install 到本地然后通过 maven 引入

### properties 配置
支持 SpringBoot properties、yml 配置

支持 beancounter.properties 配置 (请放在配置目录下)

```yml
beancounter:
    path:  扫描目录
    search-source: 查询来源 (DATABASE / MEMORY)
    store:
        interval: 文件持久化间隔时间 (s)
        path: 文件持久化路径 (不需要后缀名, 格式为 json)
    database:
        interval: 数据库持久化时间间隔(s)
        show-sql: 是否打印数据库 SQL (true / false)
        format-sql:  是否格式化数据库 SQL (true / false)
        ddl-auto:  数据库初始化方式 (CREATE / UPDATE)
        datasource: 
            url: 数据库 url
            username: 数据库用户名
            password: 数据库密码
            driver-class-name: 数据库驱动
```
注意: 数据库目前支持 MySQL 5 版本, 文件持久化请给于文件访问权限

### 循环依赖处理
目前自动检测循环依赖对于循环依赖链能检测, 但会随机给一些循环依赖链 Bean 进行 beancounter 包装

对于循环依赖, 可以使用 [``@BeanCounterExclude``](src/main/java/config/BeanCounterExclude.java) 进行排除

```java
@BeanCounterExclude
public class Circular{}
```

### 查询方法
```java
public class BeanService{
    
    @Resource
    BeanCounterService service;
    
    public void getCounterService(){
        BeanCounterParam param = new BeanCounterParam();

        param.setOrder("count");
        param.setSearchMethod(true);

        BeanCounterResult result = beanCounterService.search(param);

        result.getClassDataList(); //结果信息
    }
}
```