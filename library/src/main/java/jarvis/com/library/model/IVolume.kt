package jarvis.com.library.model

/**
 * @author yyf @ Zhihu Inc.
 * @since 05-28-2019
 */
interface IVolume {

    /**
     * 开盘价
     */
    val openPrice: Float

    /**
     * 收盘价
     */
    val closePrice: Float

    /**
     * 成交量
     */
    val volume: Float

    /**
     * 五(月，日，时，分，5分等)均量
     */
    val mA5Volume: Float

    /**
     * 十(月，日，时，分，5分等)均量
     */
    val mA10Volume: Float
}