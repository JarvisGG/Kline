package jarvis.com.library.model

/**
 * @author yyf @ Zhihu Inc.
 * @since 05-28-2019
 */
data class KLineEntity(
        var date: String? = null,
        override var openPrice: Float = 0.toFloat(),
        override var highPrice: Float = 0.toFloat(),
        override var lowPrice: Float = 0.toFloat(),
        override var closePrice: Float = 0.toFloat(),
        override var volume: Float = 0.toFloat(),
        override var mA5Price: Float = 0.toFloat(),
        override var mA10Price: Float = 0.toFloat(),
        override var mA20Price: Float = 0.toFloat(),
        override var mA30Price: Float = 0.toFloat(),
        override var mA60Price: Float = 0.toFloat(),
        override var dea: Float = 0.toFloat(),
        override var dif: Float = 0.toFloat(),
        override var macd: Float = 0.toFloat(),
        override var k: Float = 0.toFloat(),
        override var d: Float = 0.toFloat(),
        override var j: Float = 0.toFloat(),
        override var r: Float = 0.toFloat(),
        override var rsi: Float = 0.toFloat(),
        override var up: Float = 0.toFloat(),
        override var mb: Float = 0.toFloat(),
        override var dn: Float = 0.toFloat(),
        override var mA5Volume: Float = 0.toFloat(),
        override var mA10Volume: Float = 0.toFloat()

) : IKLine
