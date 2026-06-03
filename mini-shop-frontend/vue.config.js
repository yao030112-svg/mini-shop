const path = require('path')

module.exports = {
  transpileDependencies: ['@dcloudio/uni-ui'],
  configureWebpack: {
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './')
      }
    }
  },
  chainWebpack: (config) => {
    // 设置别名
    config.resolve.alias
      .set('@', path.resolve(__dirname, './'))
  }
}
