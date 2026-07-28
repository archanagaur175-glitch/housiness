package com.housiness.util

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil
import kotlin.math.min

object PdfGenerator {

    private const val RENDER_SCALE = 2.0f
    private const val PAGE_WIDTH_PT = 595f
    private const val PAGE_HEIGHT_PT = 842f

    fun generateInvoicePdf(
        context: Context,
        fileName: String,
        content: @Composable () -> Unit
    ): File {
        val pageWidthPx = (PAGE_WIDTH_PT * RENDER_SCALE).toInt()
        val pageHeightPx = (PAGE_HEIGHT_PT * RENDER_SCALE).toInt()

        val composeView = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent { content() }
            measure(
                View.MeasureSpec.makeMeasureSpec(pageWidthPx, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            layout(0, 0, measuredWidth, measuredHeight)
        }

        val fullBitmap = composeView.drawToBitmap()

        val pageCount = ceil(fullBitmap.height.toFloat() / pageHeightPx).toInt()
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH_PT.toInt(), PAGE_HEIGHT_PT.toInt(), 1)

        for (i in 0 until pageCount) {
            val page = document.startPage(pageInfo.setPageNumber(i + 1).create())
            val canvas = page.canvas

            canvas.save()
            canvas.scale(1f / RENDER_SCALE, 1f / RENDER_SCALE)

            val top = i * pageHeightPx
            val bottom = minOf((i + 1) * pageHeightPx, fullBitmap.height)
            val src = android.graphics.Rect(0, top, pageWidthPx, bottom)
            val dst = android.graphics.RectF(0f, top.toFloat(), pageWidthPx.toFloat(), bottom.toFloat())

            canvas.drawBitmap(fullBitmap, src, dst, null)
            canvas.restore()
            document.finishPage(page)
        }

        val outputDir = File(context.cacheDir, "invoices")
        outputDir.mkdirs()
        val outputFile = File(outputDir, fileName)
        FileOutputStream(outputFile).use { stream ->
            document.writeTo(stream)
        }
        document.close()
        fullBitmap.recycle()

        return outputFile
    }

    fun getShareUri(context: Context, pdfFile: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
    }

    fun getPreviewIntent(context: Context, pdfFile: File): android.content.Intent {
        val uri = getShareUri(context, pdfFile)
        return android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun getShareIntent(context: Context, pdfFile: File): android.content.Intent {
        val uri = getShareUri(context, pdfFile)
        return android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
