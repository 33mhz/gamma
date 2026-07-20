package io.pnut.gamma.presentation.activity

import android.os.Bundle
import io.pnut.gamma.databinding.ActivityFilesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilesActivity : BaseActivity() {

    private lateinit var binding: ActivityFilesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
    }

}
