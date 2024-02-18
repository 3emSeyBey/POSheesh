import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.appdev.posheesh.Classes.Products
import com.appdev.posheesh.R
import com.appdev.posheesh.ui.sales.SalesFragment
import com.squareup.picasso.Picasso
import java.io.File

class ItemListAdapter(private val context: Context, private val items: Array<Products>, private val itemClickListener: ItemClickListener) :
    RecyclerView.Adapter<ItemListAdapter.ViewHolder>() {

    interface ItemClickListener {
        fun onItemClick(item: Products)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]

        // Set item data to views
        holder.bind(currentItem)
        holder.itemView.setOnClickListener {
            flyToCartAnimation(holder.itemView, holder, currentItem)
        }
    }

    private fun flyToCartAnimation(view: View, holder: ViewHolder, currentItem: Products) {
        val itemPosition = IntArray(2)
        holder.itemView.getLocationOnScreen(itemPosition)
        val screenWidth = context.resources.displayMetrics.widthPixels/context.resources.displayMetrics.density
        val screenHeight = context.resources.displayMetrics.heightPixels/context.resources.displayMetrics.density
        val deltaX = (screenWidth)-(itemPosition[0]-view.width)
        val deltaY = (screenHeight*2.0f)-(itemPosition[1])
        Toast.makeText(context, currentItem.name+" added to cart", Toast.LENGTH_SHORT).show()
        val translateAnimation = TranslateAnimation(0f, deltaX, 0f, deltaY)
        translateAnimation.duration = 500
        val scaleAnimation = ScaleAnimation(1.0f, 0.1f, 1.0f, 0.1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        scaleAnimation.duration = 1000
        val alphaAnimation = AlphaAnimation(1.0f, 0.0f)
        alphaAnimation.duration = 300
        val animationSet = AnimationSet(true)
        animationSet.addAnimation(translateAnimation)
        animationSet.addAnimation(scaleAnimation)
        animationSet.addAnimation(alphaAnimation)
        animationSet.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                SalesFragment.addToCart(currentItem.code, 1)
            }

            override fun onAnimationEnd(animation: Animation) {

            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        view.startAnimation(animationSet)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }
        private val imageItemImageView: ImageView = itemView.findViewById(R.id.imageItem)
        private val itemNameTextView: TextView = itemView.findViewById(R.id.textItemName)
        private val itemPriceTextView: TextView = itemView.findViewById(R.id.textPrice)

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val item = items[position]
                itemClickListener.onItemClick(item)
            }
        }

        fun bind(item: Products) {
            // Bind item data to views
            Picasso.get().load(File(item.imageUri)).into(imageItemImageView)
            itemNameTextView.text = item.name
            itemPriceTextView.text = "Price: ${item.sellingPrice}"
            // You can set other item details here if needed
        }
    }
}
