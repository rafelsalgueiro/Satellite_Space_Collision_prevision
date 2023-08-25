package utilities
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.satellite_space_collision_prevision.databinding.PopupInfoBinding

class InfoPopupDialog: DialogFragment() {
    private lateinit var binding: PopupInfoBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = PopupInfoBinding.inflate(LayoutInflater.from(requireContext()))

        binding.ivLinkedin.setOnClickListener {
            val linkedinProfileUrl = "https://www.linkedin.com/in/rafel-salgueiro-134435236/"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkedinProfileUrl))
            startActivity(intent)
        }

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)
        return builder.create()
    }
}