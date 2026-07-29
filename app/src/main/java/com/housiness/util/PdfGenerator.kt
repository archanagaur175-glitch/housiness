package com.housiness.util

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.housiness.data.db.entity.Invoice
import com.housiness.data.db.entity.InvoiceLineItem
import com.housiness.util.CurrencyFormatter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


object PdfGenerator {

    private const val PAGE_WIDTH_PT = 595f
    private const val PAGE_HEIGHT_PT = 842f
    private const val MARGIN = 50f
    private const val LINE_HEIGHT = 20f

    fun generateInvoicePdf(
        context: Context,
        fileName: String,
        invoice: Invoice,
        lineItems: List<InvoiceLineItem>,
        businessName: String = "Your Business Name"
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH_PT.toInt(), PAGE_HEIGHT_PT.toInt(), 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var y = MARGIN + 10f

        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 32f
            textAlign = Paint.Align.CENTER
        }

        val refPaint = Paint().apply {
            color = 0xFF1976D2.toInt()
            textSize = 18f
            textAlign = Paint.Align.CENTER
        }

        val boldPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 14f
        }

        val normalPaint = Paint().apply {
            textSize = 14f
        }

        val smallPaint = Paint().apply {
            textSize = 12f
        }

        val headerPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 14f
        }

        val dividerPaint = Paint().apply {
            strokeWidth = 2f
        }

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        // Title
        canvas.drawText("INVOICE", PAGE_WIDTH_PT / 2f, y, titlePaint)
        y += 40f
        canvas.drawText(invoice.orderRef, PAGE_WIDTH_PT / 2f, y, refPaint)
        y += 30f

        // Divider
        canvas.drawLine(MARGIN, y, PAGE_WIDTH_PT - MARGIN, y, dividerPaint)
        y += 20f

        // Business info
        canvas.drawText(businessName, MARGIN, y, boldPaint)
        y += 20f
        canvas.drawText("Date: ${sdf.format(Date(invoice.generatedAt))}", MARGIN, y, smallPaint)
        y += 25f

        // Divider
        dividerPaint.strokeWidth = 1f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH_PT - MARGIN, y, dividerPaint)
        y += 20f

        // Table header
        val col1 = MARGIN
        val col2 = PAGE_WIDTH_PT - MARGIN - 280f
        val col3 = PAGE_WIDTH_PT - MARGIN - 150f
        val col4 = PAGE_WIDTH_PT - MARGIN - 80f

        canvas.drawText("Item", col1, y, headerPaint)
        canvas.drawText("Qty", col2, y, headerPaint)
        canvas.drawText("Price", col3, y, headerPaint)
        canvas.drawText("Total", col4 + 20f, y, headerPaint)
        y += 20f

        dividerPaint.strokeWidth = 1f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH_PT - MARGIN, y, dividerPaint)
        y += 15f

        // Line items
        val itemPaint = TextPaint().apply {
            textSize = 14f
        }

        for (item in lineItems) {
            val nameLayout = StaticLayout.Builder.obtain(item.productName, 0, item.productName.length, itemPaint, (col2 - col1).toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1f)
                .setMaxLines(2)
                .build()

            canvas.save()
            canvas.translate(col1, y)
            nameLayout.draw(canvas)
            canvas.restore()

            val nameHeight = nameLayout.height.coerceAtLeast(LINE_HEIGHT.toInt())

            canvas.drawText("${item.quantity}", col2, y + 15f, normalPaint)
            canvas.drawText(CurrencyFormatter.format(item.unitPrice), col3, y + 15f, normalPaint)
            canvas.drawText(CurrencyFormatter.format(item.total), col4 + 20f, y + 15f, normalPaint)

            y += nameHeight + 8f
        }

        y += 10f

        // Divider
        dividerPaint.strokeWidth = 2f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH_PT - MARGIN, y, dividerPaint)
        y += 25f

        // Total
        val totalPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = 0xFF1976D2.toInt()
            textSize = 22f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("Total: ${CurrencyFormatter.format(invoice.subtotal)}", PAGE_WIDTH_PT - MARGIN, y, totalPaint)

        // Footer
        y = PAGE_HEIGHT_PT - MARGIN - 10f
        val footerPaint = Paint().apply {
            textSize = 14f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Thank you for your order!", PAGE_WIDTH_PT / 2f, y, footerPaint)

        document.finishPage(page)

        val outputDir = File(context.cacheDir, "invoices")
        outputDir.mkdirs()
        val outputFile = File(outputDir, fileName)
        FileOutputStream(outputFile).use { stream ->
            document.writeTo(stream)
        }
        document.close()

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
