package jarvis.com.kline.net

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import jarvis.com.library.helper.DataHelper
import jarvis.com.library.model.KLineEntity
import java.util.ArrayList

/**
 * @author yyf @ Zhihu Inc.
 * @since 05-28-2019
 */
internal object DataRequest {
    private var datas: List<KLineEntity>? = null

    fun getStringFromAssert(context: Context, fileName: String): String {
        try {
            val `in` = context.resources.assets.open(fileName)
            val length = `in`.available()
            val buffer = ByteArray(length)
            `in`.read(buffer)
            return String(buffer, 0, buffer.size, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    fun getALL(context: Context): List<KLineEntity>? {
        if (datas == null) {
            val data = Gson().fromJson<List<KLineEntity>>(getStringFromAssert(context, "ibm.json"), object : TypeToken<List<KLineEntity>>() {
            }.getType())
            DataHelper.calculate(data)
            datas = data
        }
        return datas
    }

    /**
     * 分页查询
     *
     * @param context
     * @param offset  开始的索引
     * @param size    每次查询的条数
     */
    fun getData(context: Context, offset: Int, size: Int): List<KLineEntity> {
        val all = getALL(context)
        val data = ArrayList<KLineEntity>()
        val start = Math.max(0, all!!.size - 1 - offset - size)
        val stop = Math.min(all.size, all.size - offset)
        for (i in start until stop) {
            data.add(all[i])
        }
        return data
    }
}