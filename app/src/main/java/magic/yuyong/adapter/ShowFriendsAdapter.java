package magic.yuyong.adapter;

import java.util.List;

import magic.yuyong.R;
import magic.yuyong.model.User;
import magic.yuyong.transformation.CircleTransformation;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class ShowFriendsAdapter extends BaseAdapter{
	private List<User> friends;
	private Context mContext;
	private LayoutInflater inflater;
	

	public ShowFriendsAdapter(Context content) {
		super();
		mContext = content;
		inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void addData(List<User> friends){
		if(this.friends == null){
			this.friends = friends;
		}else{
			this.friends.addAll(friends);
		}
		notifyDataSetChanged();
	}
	
	public List<User> getData(){
		return friends;
	}

	@Override
	public int getCount() {
		return friends ==  null ? 0 : friends.size();
	}

	@Override
	public Object getItem(int arg0) {
		return friends ==  null ? null : friends.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}
	
	private class ViewHolder{
		ImageView user_avatar;
		TextView user_name;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final User friend = friends.get(position);
		ViewHolder holder = null;
		if(convertView == null){
			convertView = inflater.inflate(R.layout.friend_item, null);
			holder = new ViewHolder();
			holder.user_avatar = (ImageView) convertView.findViewById(R.id.user_avatar);
			holder.user_name = (TextView)convertView.findViewById(R.id.user_name);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		String avatar_url = friend.getProfile_image_url();
		Picasso.with(mContext).load(avatar_url).placeholder(R.drawable.avatar).transform(new CircleTransformation()).into(holder.user_avatar);
		holder.user_name.setText(friend.getScreen_name());
		
		return convertView;
	}

}
