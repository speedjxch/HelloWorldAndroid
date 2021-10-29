package com.example.recyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedList;

public class WordListAdapter
        extends RecyclerView.Adapter<WordListAdapter.WordViewHolder>
        implements Filterable {

    //private final LinkedList<String> mWordList;
    private LinkedList<String> mWordList;
    private LayoutInflater mInflater;
    private ArrayList<String> mOriginWordList;
    private ArrayFilter mFilter;
    private final Object mLock = new Object();

    public void add_mOriginWordList(String t){
        mOriginWordList.add(t);
        return;
    }

    class ArrayFilter extends Filter {
        /**
         * 执行过滤的方法
         * @param prefix
         * @return
         */
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            // 过滤的结果
            FilterResults results = new FilterResults();
            // 原始数据备份为空时，上锁，同步复制原始数据
            if (mOriginWordList == null) {
                synchronized (mLock) {
                    mOriginWordList = new ArrayList<>(mWordList);
                }
            }
            // 当首字母为空时
            if (prefix == null || prefix.length() == 0) {
                ArrayList<String> list;
                // 同步复制一个原始备份数据
                synchronized (mLock) {
                    list = new ArrayList<>(mOriginWordList);
                }
                // 此时返回的results就是原始的数据，不进行过滤
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();

                ArrayList<String> values;
                // 同步复制一个原始备份数据
                synchronized (mLock) {
                    values = new ArrayList<>(mOriginWordList);
                }
                final int count = values.size();
                final ArrayList<String> newValues = new ArrayList<>();

                for (int i = 0; i < count; i++) {
                    // 从List<TaskModel>中拿到TaskModel对象
                    final String value = values.get(i);
                    // TaskModel对象的任务名称属性作为过滤的参数
                    final String valueText = value.toString().toLowerCase();
                    // 关键字是否和item的过滤参数匹配
                    if (valueText.indexOf(prefixString.toString()) != -1) {
                        // 将这个item加入到数组对象中
                        newValues.add(value);
                    } else {
                        // 处理首字符是空格
                        final String[] words = valueText.split(" ");
                        final int wordCount = words.length;

                        for (int k = 0; k < wordCount; k++) {
                            // 一旦找到匹配的就break，跳出for循环
                            if (words[k].indexOf(prefixString) != -1) {
                                newValues.add(value);
                                break;
                            }
                        }
                    }
                }
                // 此时的results就是过滤后的List<TaskModel>数组
                results.values = newValues;
                results.count = newValues.size();
            }
            return results;
        }

        /**
         * 得到过滤结果
         *
         * @param prefix
         * @param results
         */
        @Override
        protected void publishResults(CharSequence prefix, FilterResults results) {
            // 此时，Adapter数据源就是过滤后的Results
            mWordList.clear();
            mWordList.addAll((ArrayList<String>) results.values);
            if (results.count > 0) {
                // 这个相当于从mDatas中删除了一些数据，只是数据的变化，故使用notifyDataSetChanged()
                notifyDataSetChanged();
            } else {
                // 当results.count<=0时，此时数据源就是重新new出来的，说明原始的数据源已经失效了
                //notifyDataSetInvalidated();
                mWordList.clear();
                mWordList.addAll((ArrayList<String>) mOriginWordList);
            }
        }
    }


    public WordListAdapter(Context context,
                           LinkedList<String> wordList) {
        mInflater = LayoutInflater.from(context);
        this.mWordList = wordList;

    }


    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }


    class WordViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        public final TextView wordItemView;
        final WordListAdapter mAdapter;

        public WordViewHolder(View itemView, WordListAdapter adapter) {
            super(itemView);
            wordItemView = itemView.findViewById(R.id.word);
            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Get the position of the item that was clicked.
            int mPosition = getLayoutPosition();

            // Use that to access the affected item in mWordList.
            String element = mWordList.get(mPosition);
            // Change the word in the mWordList.

            mWordList.set(mPosition, "Clicked! " + element);
            // Notify the adapter, that the data has changed so it can
            // update the RecyclerView to display the data.
            mAdapter.notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public WordListAdapter.WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.wordlist_item,
                parent, false);
        return new WordViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull WordListAdapter.WordViewHolder holder, int position) {
        String mCurrent = mWordList.get(position);
        holder.wordItemView.setText(mCurrent);
    }

    @Override
    public int getItemCount() {
        return mWordList.size();
    }
}
