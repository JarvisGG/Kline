package jarvis.com.library.model

/**
 * @author yyf @ Zhihu Inc.
 * @since 05-28-2019
 * @descripe KDJ指标(随机指标)接口
 * 相关说明:https://baike.baidu.com/item/KDJ%E6%8C%87%E6%A0%87/6328421?fr=aladdin&fromid=3423560&fromtitle=kdj
 */
interface IKDJ {

    /**
     * K值
     */
    val k: Float

    /**
     * D值
     */
    val d: Float

    /**
     * J值
     */
    val j: Float

}