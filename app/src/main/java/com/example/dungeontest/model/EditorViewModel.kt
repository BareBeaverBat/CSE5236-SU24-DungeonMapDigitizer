package com.example.dungeontest.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class EditorViewModel( application: Application): AndroidViewModel(application) {

    val testDot = "digraph G {\n" +
            "  node0;\n" +
            "  node1;\n" +
            "  node2;\n" +
            "  node3;\n" +
            "  node4;\n" +
            "  node5;\n" +
            "  node6;\n" +
            "  node7;\n" +
            "  node8;\n" +
            "  node9;\n" +
            "  node10;\n" +
            "  node11;\n" +
            "  node12;\n" +
            "  node13;\n" +
            "  node14;\n" +
            "\n" +
            "  node0 -> node5;\n" +
            "  node1 -> node8;\n" +
            "  node2 -> node11;\n" +
            "  node3 -> node1;\n" +
            "  node4 -> node9;\n" +
            "  node5 -> node14;\n" +
            "  node6 -> node3;\n" +
            "  node7 -> node0;\n" +
            "  node8 -> node12;\n" +
            "  node9 -> node6;\n" +
            "  node10 -> node2;\n" +
            "  node11 -> node7;\n" +
            "  node12 -> node4;\n" +
            "  node13 -> node10;\n" +
            "  node14 -> node13;\n" +
            "}"



}