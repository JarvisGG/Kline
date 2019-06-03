package jarvis.com.library.ui.draw

/**
 * @author yyf @ Zhihu Inc.
 * @since 05-31-2019
 */
interface IValueFormatter {
    /**
     * 格式化value
     *
     * @param value 传入的value值
     * @return 返回字符串
     */
    fun format(value: Float): String
}