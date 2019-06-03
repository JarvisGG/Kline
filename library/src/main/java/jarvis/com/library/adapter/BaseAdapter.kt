package jarvis.com.library.adapter

import android.database.DataSetObservable
import android.database.DataSetObserver

/**
 * @author yyf @ Zhihu Inc.
 * @since 05-31-2019
 */
abstract class KLineChartAdapter : IAdapter {
    var dataObservable = DataSetObservable()

    fun notifyDataSetChanged() {
        if (getCount() > 0) {
            mDataSetObservable.notifyChanged()
        } else {
            mDataSetObservable.notifyInvalidated()
        }
    }


    fun registerDataSetObserver(observer: DataSetObserver) {
        mDataSetObservable.registerObserver(observer)
    }

    fun unregisterDataSetObserver(observer: DataSetObserver) {
        mDataSetObservable.unregisterObserver(observer)
    }

}