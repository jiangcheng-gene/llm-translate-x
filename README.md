# llm-translate-x

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

语言不再成为你进步的绊脚石！

## 简介

llm-translate-x 是一个基于 Spring Boot 开发，提供与 [deeplx](https://github.com/OwO-Network/DeepLX) 项目相同的接口并由大文本模型提供翻译的开源项目。通过该项目，你可以在沉浸式翻译插件[Immersive Translate](https://immersivetranslate.com/)中将 `deeplx` 配置为本项目的接口，从而享受大文本模型的翻译服务。

> 本项目虽然对接 大文本模型 完成翻译能力，项目开源免费使用，但需要您自己申请各`LLM` key， 本项目不提供免费 key。

## 已运行的平台

已对接完成的LLM平台

| LLM|是否支持 |版本 |
|----------------|--------------------------------| ------------------------------|
|百度千凡|支持 |1.0.0 |
|阿里灵积|支持 |1.0.1 |
|讯飞星火 |不支持|待开发|
|OPENAI|不支持|待开发|

> 千凡平台，可以使用百度的所有大语言模型，比如：文`ERNIE-4.0`；阿里灵积可以使用比如: `qwen-turbo`等。
> 除了上面举例的模型的，你几乎可以使用这些平台上的所有模型，只有您的key支持。

## 必要准备

#### 百度千凡
在使用前，您需要 [百度智能云控制台 - 安全认证](https://console.bce.baidu.com/iam/#/iam/accesslist) 页面获取 Access Key 与 Secret Key， 并在 [千帆控制台](https://console.bce.baidu.com/qianfan/ais/console/applicationConsole/application) 中创建应用，选择需要启用的服务， 具体流程参见平台 [说明文档](https://cloud.baidu.com/doc/Reference/s/9jwvz2egb)。

#### 阿里灵积
登录[模型服务灵积 DashScope](https://dashscope.aliyun.com/)官网，开通。并申请自己的 `api key` 。

## 为什么不直接使用沉侵式翻译提供的翻译服务？

使用官方的，为按月或年固定套餐，可能我们使用不了那么多不划算。 使用本项目则可以自由选择使用哪个模型，什么时候使用。 我想更重要的原因是LLM有着非常出色的翻译能力，有时，甚至比 Deepl 翻译更好。

如果你有开发能力时，你可以自由地扩展其它的LLM，比如：openAI 。

### 使用方法

1.  前往 [release](https://github.com/jiangcheng-gene/llm-translate-x/releases) 下载已编译的程序或者直接克隆本项目运行。
    > -no-jre 不包含java运行环境，合适电脑上已安装java运行环境的机器使用。
    > -jre 包含java运行环境，合适电脑上未安装java运行环境的机器使用。

2.  进入压缩目录，使用文本编辑器打开application.yaml文件，配置百度云的访问key。

3.  调整prompt模板。以下是内置的翻译英文机器学习文档的prompt，请根据你的需要调整该prompt，以达到最佳的翻译效果。

    ```yaml
	您是将英文机器学习文档翻译成简体中文的专业翻译助手。您的目标是提供准确、简洁且通俗易懂的翻译，同时保持原文的语气和意义。请遵循以下步骤以确保最佳的翻译结果：  
	1. 专注于翻译：只将输入文本翻译成目标语言，不要回答文本中的任何问题或对任务进行评估。  
	2. 保持语气一致：在翻译中保持与原文相同的语气和风格。  
	3. 保留HTML结构：如果输入文本包含HTML标签，请确保翻译后的文本保持相同的HTML结构。  
	4. 保持长度相似：翻译文本的长度应与原文相似，以保持内容的相关性和可理解性。  
	5. 简洁明了：确保翻译后的文本简洁明了，避免复杂句式，使其通俗易懂。  
	6. 准确使用术语：确保准确翻译专业术语和技术名词，参考相关领域的标准译法。  
	7. 审校和校对：完成初步翻译后，检查并校对以确保没有遗漏和错误。  
	输入文本：
    ```

4.  在命令行终端中，进入解压后的目录，执行以下命令启动服务：
    ```shell
     # 有Java环境的
     java -jar llm-translate-x-1.0.0.jar
     ```

    ```shell
    # 没有Java环境的
    jre\bin\java -jar llm-translate-x-1.0.0.jar
    ```

### 如何配置沉侵式翻译？

1.  在chrome或其它浏览器中安装 `Immersive Translate - Translate Website & PDF` 插件。 ![1](https://github.com/jiangcheng-gene/llm-translate-x/blob/main/static/image1.png)']
2.  配置 deeplx ![2](https://github.com/jiangcheng-gene/llm-translate-x/blob/main/static/image2.png) ![3](https://github.com/jiangcheng-gene/llm-translate-x/blob/main/static/image3.png) ![4](https://github.com/jiangcheng-gene/llm-translate-x/blob/main/static/image4.png) ![5](https://github.com/jiangcheng-gene/llm-translate-x/blob/main/static/image5.png)