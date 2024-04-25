import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
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
            highlightContainer(holder.itemView, holder, currentItem)
        }
    }

    private fun highlightContainer(view: View, holder: ViewHolder, currentItem: Products) {
        val highlightScale = 1.1f // Increase the scale slightly for highlighting effect

        // Highlighting animation
        val scaleAnimation = ScaleAnimation(1.0f, highlightScale, 1.0f, highlightScale,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f)
        scaleAnimation.duration = 200 // Adjust duration as needed

        // Set up animation set
        val animationSet = AnimationSet(true)
        animationSet.addAnimation(scaleAnimation)

        animationSet.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                // You can add any action needed when animation starts
                Toast.makeText(context, currentItem.name+" added to cart", Toast.LENGTH_SHORT).show()
                val borderColor = Color.parseColor("#DAA06D")
                val backgroundColor = Color.parseColor("#E1C16E")

                // Create a GradientDrawable for the background
                val backgroundDrawable = GradientDrawable()
                backgroundDrawable.shape = GradientDrawable.RECTANGLE
                backgroundDrawable.cornerRadius = 50f // Adjust the corner radius as needed
                backgroundDrawable.setColor(backgroundColor)

                // Set the border color and width
                backgroundDrawable.setStroke(2, borderColor) // Adjust the width as needed

                // Set the background drawable to the view
                view.background = backgroundDrawable

                SalesFragment.addToCart(currentItem.code, 1)
            }

            override fun onAnimationEnd(animation: Animation) {
                // Perform any action needed after the animation ends
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        // Start the animation on the provided view
        view.startAnimation(animationSet)

        // If you want to trigger any other action when the container is clicked, add it here
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
