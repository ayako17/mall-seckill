// src/utils/ossHelper.js
import OSS from 'ali-oss';

const client = new OSS({
  region: 'oss-cn-hangzhou',
  accessKeyId: import.meta.env.VITE_OSS_ACCESS_KEY_ID,  // Vite 会自动加载 .env.local
  accessKeySecret: import.meta.env.VITE_OSS_ACCESS_KEY_SECRET,
  bucket: 'mall-seckill-oss'
});

/**
 * 生成可访问的图片URL（带签名）
 * @param {string} objectKey - OSS中的文件路径，例如 'images/iphone15.jpg'
 * @param {number} expires - 过期时间（秒），默认3600（1小时）
 * @returns {string} 带签名的URL
 */
export function getSignedImageUrl(objectKey, expires = 3600) {
  try {
    const url = client.signatureUrl(objectKey, {
      expires, // URL有效时间
      // 可选：对图片进行实时处理，比如缩放
      // process: 'image/resize,w_200' 
    });
    return url;
  } catch (error) {
    console.error('生成签名URL失败:', error);
    return '';
  }
}