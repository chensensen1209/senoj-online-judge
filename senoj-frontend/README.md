# senoj-frontend

## Project setup

```
npm install
```

### Compiles and hot-reloads for development

```
npm run serve
```

### Compiles and minifies for production

```
npm run build
```

### Lints and fixes files

```
npm run lint
```

### Customize configuration

See [Configuration Reference](https://cli.vuejs.org/config/).

## 根据后台生成代码

每次都去openapi文件中改
WITH_CREDENTIALS: true,

``` shell
openapi --input http://localhost:8101/api/v2/api-docs --output ./generated --client axios
