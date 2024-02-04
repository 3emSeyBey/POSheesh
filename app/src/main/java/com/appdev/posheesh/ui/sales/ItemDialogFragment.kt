import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.appdev.posheesh.R

class ItemDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(itemName: String): ItemDialogFragment {
            val fragment = ItemDialogFragment()
            val args = Bundle()
            args.putString("itemName", itemName)
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_item_options, null)

        // Set the view for the dialog
        builder.setView(view)

        // Add other configurations like title, buttons, etc.

        return builder.create()
    }
}
