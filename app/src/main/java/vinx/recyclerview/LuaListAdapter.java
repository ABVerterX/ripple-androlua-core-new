package vinx.recyclerview;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.luajava.LuaTable;

import java.lang.reflect.Field;
import java.util.List;

public class LuaListAdapter extends ListAdapter<LuaTable, LuaViewHolder> {


    public static final String TAG = "LuaListAdapter";

    private AsyncListDiffer differ;

    public interface LuaInterface {
        View onCreateViewHolder(ViewGroup p1, int p2);

        void onBindViewHolder(Object views, Object data, LuaListAdapter self, LuaViewHolder p1, int p2);

        boolean areItemsTheSame(LuaTable p1, LuaTable p2);

        int getItemViewType(int position);

        boolean areContentsTheSame(LuaTable p1, LuaTable p2);
    }

    public static class DiffUtilItemCallback extends DiffUtil.ItemCallback<LuaTable> {

        private LuaInterface interfaces;

        public DiffUtilItemCallback(LuaInterface interfaces) {
            this.interfaces = interfaces;
        }

        @Override
        public boolean areItemsTheSame(LuaTable p1, LuaTable p2) {
            return interfaces.areItemsTheSame(p1, p2);
        }

        @Override
        public boolean areContentsTheSame(LuaTable p1, LuaTable p2) {
            return interfaces.areContentsTheSame(p1, p2);
        }


    }

    private LuaInterface interfaces;

    public LuaListAdapter(LuaInterface interfaces) {
        super(new DiffUtilItemCallback(interfaces));
        this.interfaces = interfaces;
        Class parent = ListAdapter.class;
        try {
            Field field = parent.getDeclaredField("mDiffer");
            field.setAccessible(true);
            differ = (AsyncListDiffer) field.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
        }
    }

    @Override
    public int getItemViewType(int position) {

        return (int) interfaces.getItemViewType(position);
    }

    @Override
    public LuaViewHolder onCreateViewHolder(ViewGroup p1, int p2) {
        return new LuaViewHolder(interfaces.onCreateViewHolder(p1, p2));
    }

    @Override
    public void onBindViewHolder(@NonNull LuaViewHolder viewHolder, int position) {
        interfaces.onBindViewHolder(
                viewHolder.itemView.getTag(),
                this.getCurrentList().get(position),
                this,
                viewHolder,
                position);
    }


    public List getCurrentList() {
        return differ.getCurrentList();
    }


}
