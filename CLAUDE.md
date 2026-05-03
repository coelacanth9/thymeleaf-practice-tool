# 配布時の注意

JAR をビルドして配布する際は、プロファイルを `prod` に切り替えること。
`dev` プロファイルが有効なままだとステージエディター (`/editor`) が公開される。

```
java -jar app.jar --spring.profiles.active=prod
```
