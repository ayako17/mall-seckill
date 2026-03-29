package com.seckill.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品 ES 文档实体
 * 用于全文搜索、分词、高亮显示
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products")
public class ProductDocument {
    
    @Id
    private Long id;
    
    // 商品名称（支持中文分词）
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String name;
    
    // 商品描述（支持中文分词）
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String description;
    
    // 价格（不分词）
    @Field(type = FieldType.Double)
    private BigDecimal price;
    
    // 库存（不分词）
    @Field(type = FieldType.Integer)
    private Integer stock;
    
    // 状态（不分词）
    @Field(type = FieldType.Integer)
    private Integer status;
    
    // 创建时间
    @Field(type = FieldType.Date)
    private LocalDateTime createTime;
    
    // 更新时间
    @Field(type = FieldType.Date)
    private LocalDateTime updateTime;
}
