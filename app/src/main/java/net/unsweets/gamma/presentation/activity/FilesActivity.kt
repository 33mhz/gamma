package net.unsweets.gamma.presentation.activity

import android.os.Bundle
import net.unsweets.gamma.databinding.ActivityFilesBinding

class FilesActivity : BaseActivity() {

    private lateinit var binding: ActivityFilesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
    }

}
