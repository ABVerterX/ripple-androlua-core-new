package vinx.material.textfield

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.text.*
import android.text.method.KeyListener
import android.text.method.MovementMethod
import android.text.method.TransformationMethod
import android.text.style.URLSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ActionMode
import android.view.ViewDebug.ExportedProperty
import android.view.accessibility.AccessibilityNodeInfo
import android.view.inputmethod.CompletionInfo
import android.view.inputmethod.CorrectionInfo
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.view.textclassifier.TextClassifier
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.TextView.BufferType
import androidx.annotation.DrawableRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.graphics.toColor
import androidx.core.view.ContentInfoCompat
import androidx.core.widget.TextViewCompat
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.util.*

open class MaterialTextField @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = getResourcesId("Widget_MaterialComponents_TextInputLayout_OutlinedBox")
) : TextInputLayout(
    ContextThemeWrapper(context, resolveStyleAttr(defStyleAttr)), attrs, resolveStyleAttr(defStyleAttr)
) {
    private var mContext: Context? = context
    private var mEditText: EditText
    private var layoutMode: Int = 0

//    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    companion object {
        private val MDC_STYLE_RESOURCES_CLASS =
            Class.forName("com.google.android.material.R${'$'}style")

        private val MDC_ATTR_RESOURCES_CLASS =
            Class.forName("com.google.android.material.R${'$'}attr")

        private fun getResourcesId(style: String): Int {
            return MDC_STYLE_RESOURCES_CLASS.getDeclaredField(style)
                .getInt(MDC_STYLE_RESOURCES_CLASS)
        }

        private fun getAttributesId(attr: String): Int {
            return MDC_ATTR_RESOURCES_CLASS.getDeclaredField(attr).getInt(MDC_ATTR_RESOURCES_CLASS)
        }

        private fun resolveStyleAttr(defStyleAttr: Int): Int {
            return when (defStyleAttr) {
                getResourcesId("Widget_MaterialComponents_TextInputLayout_OutlinedBox_ExposedDropdownMenu"),
                getResourcesId("Widget_MaterialComponents_TextInputLayout_OutlinedBox_Dense_ExposedDropdownMenu"),
                getResourcesId("Widget_Material3_TextInputLayout_OutlinedBox_ExposedDropdownMenu"),
                getResourcesId("Widget_Material3_TextInputLayout_OutlinedBox_Dense_ExposedDropdownMenu")
                -> getAttributesId("textInputOutlinedExposedDropdownMenuStyle")

                getResourcesId("Widget_MaterialComponents_TextInputLayout_FilledBox_ExposedDropdownMenu"),
                getResourcesId("Widget_MaterialComponents_TextInputLayout_FilledBox_Dense_ExposedDropdownMenu"),
                getResourcesId("Widget_Material3_TextInputLayout_FilledBox_ExposedDropdownMenu"),
                getResourcesId("Widget_Material3_TextInputLayout_FilledBox_Dense_ExposedDropdownMenu")
                -> getAttributesId("textInputFilledExposedDropdownMenuStyle")

                else -> defStyleAttr
            }
        }

        const val LAYOUT_MODE_OUTLINED = 1
        const val LAYOUT_MODE_OUTLINED_AUTOCOMPLETE = 2
        const val LAYOUT_MODE_FILLED = 3
        const val LAYOUT_MODE_FILLED_AUTOCOMPLETE = 4
        const val LAYOUT_MODE_NORMAL = 5
        const val LAYOUT_MODE_NORMAL_AUTOCOMPLETE = 6
    }

    init {
        when (defStyleAttr) {
            getResourcesId("Widget_MaterialComponents_TextInputLayout_OutlinedBox"),
            getResourcesId("Widget_MaterialComponents_TextInputLayout_OutlinedBox_Dense"),
            getResourcesId("Widget_Material3_TextInputLayout_OutlinedBox"),
            getResourcesId("Widget_Material3_TextInputLayout_OutlinedBox_Dense") -> {
                layoutMode = LAYOUT_MODE_OUTLINED
                boxBackgroundMode = BOX_BACKGROUND_OUTLINE
                boxStrokeColor = 0xFF6200EE.toInt()
                setBoxCornerRadii(4.dp2px(), 4.dp2px(), 4.dp2px(), 4.dp2px())
                mEditText = TextInputEditText(this.context)
            }

            getResourcesId("Widget_MaterialComponents_TextInputLayout_OutlinedBox_ExposedDropdownMenu"),
            getResourcesId("Widget_MaterialComponents_TextInputLayout_OutlinedBox_Dense_ExposedDropdownMenu"),
            getResourcesId("Widget_Material3_TextInputLayout_OutlinedBox_ExposedDropdownMenu"),
            getResourcesId("Widget_Material3_TextInputLayout_OutlinedBox_Dense_ExposedDropdownMenu"),
            getAttributesId("textInputOutlinedExposedDropdownMenuStyle") -> {
                layoutMode = LAYOUT_MODE_OUTLINED_AUTOCOMPLETE
                boxBackgroundMode = BOX_BACKGROUND_OUTLINE
                boxStrokeColor = 0xFF6200EE.toInt()
                setBoxCornerRadii(4.dp2px(), 4.dp2px(), 4.dp2px(), 4.dp2px())
                mEditText = MaterialAutoCompleteTextView(this.context)
            }

            getResourcesId("Widget_MaterialComponents_TextInputLayout_FilledBox"),
            getResourcesId("Widget_MaterialComponents_TextInputLayout_FilledBox_Dense"),
            getResourcesId("Widget_Material3_TextInputLayout_FilledBox"),
            getResourcesId("Widget_Material3_TextInputLayout_FilledBox_Dense") -> {
                layoutMode = LAYOUT_MODE_FILLED
                boxBackgroundMode = BOX_BACKGROUND_FILLED
                boxStrokeColor = 0xFF6200EE.toInt()
                setBoxCornerRadii(4.dp2px(), 4.dp2px(), 0F, 0F)
                mEditText = TextInputEditText(this.context)
            }

            getResourcesId("Widget_MaterialComponents_TextInputLayout_FilledBox_ExposedDropdownMenu"),
            getResourcesId("Widget_MaterialComponents_TextInputLayout_FilledBox_Dense_ExposedDropdownMenu"),
            getResourcesId("Widget_Material3_TextInputLayout_FilledBox_ExposedDropdownMenu"),
            getResourcesId("Widget_Material3_TextInputLayout_FilledBox_Dense_ExposedDropdownMenu"),
            getAttributesId("textInputFilledExposedDropdownMenuStyle") -> {
                layoutMode = LAYOUT_MODE_FILLED_AUTOCOMPLETE
                boxBackgroundMode = BOX_BACKGROUND_FILLED
                boxStrokeColor = 0xFF6200EE.toInt()
                setBoxCornerRadii(4.dp2px(), 4.dp2px(), 0F, 0F)
                mEditText = MaterialAutoCompleteTextView(this.context)
            }

            else -> {
                layoutMode = LAYOUT_MODE_NORMAL
                boxBackgroundMode = BOX_BACKGROUND_NONE
                boxStrokeColor = 0xFF6200EE.toInt()
                mEditText = TextInputEditText(this.context)
            }
        }
        addView(mEditText)
    }

    override fun getEditText(): EditText {
        return mEditText
    }

    private fun Int.dp2px(): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), mContext?.resources?.displayMetrics
        )
    }

    fun setTintColor(color: Int) {
        boxStrokeColor = color
        hintTextColor = ColorStateList.valueOf(color)
        if (Build.VERSION.SDK_INT >= 26) {
            val colorObj = color.toColor()
            setHighlightColor(
                Color.argb(
                    0.3F, colorObj.red(), colorObj.green(), colorObj.blue()
                )
            )
        }
        if (Build.VERSION.SDK_INT >= 29) {
            getTextCursorDrawable()?.setTint(color)
            getTextSelectHandle()?.setTint(color)
            getTextSelectHandleLeft()?.setTint(color)
            getTextSelectHandleRight()?.setTint(color)
        }
    }

    fun setFocusedRectEnabled(textInputLayoutFocusedRectEnabled: Boolean) {
        (mEditText as TextInputEditText).isTextInputLayoutFocusedRectEnabled =
            textInputLayoutFocusedRectEnabled
    }

    fun isFocusedRectEnabled(): Boolean {
        return (mEditText as TextInputEditText).isTextInputLayoutFocusedRectEnabled
    }

    fun getEditTextFocusedRect(r: Rect?) {
        return mEditText.getFocusedRect(r)
    }

    fun getEditTextGlobalVisibleRect(r: Rect?, globalOffset: Point?): Boolean {
        return mEditText.getGlobalVisibleRect(r, globalOffset)
    }

    fun requestEditTextRectangleOnScreen(rectangle: Rect?): Boolean {
        return mEditText.requestRectangleOnScreen(rectangle)
    }

    fun onEditTextInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        return mEditText.onInitializeAccessibilityNodeInfo(info)
    }

    fun getText(): Editable? {
        return mEditText.text
    }

    fun setEditTextBackgroundResource(@DrawableRes resId: Int) {
        mEditText.setBackgroundResource(resId)
    }

    fun setEditTextBackgroundDrawable(background: Drawable?) {
        mEditText.background = background
    }

    fun setTextAppearance(context: Context?, resId: Int) {
        context.let {}
        TextViewCompat.setTextAppearance(mEditText, resId)
    }

    fun setCustomSelectionActionModeCallback(actionModeCallback: ActionMode.Callback?) {
        mEditText.customSelectionActionModeCallback = actionModeCallback
    }

    fun getCustomSelectionActionModeCallback(): ActionMode.Callback? {
        return mEditText.customSelectionActionModeCallback
    }

    fun setTextClassifier(textClassifier: TextClassifier?) {
        if (Build.VERSION.SDK_INT >= 26) mEditText.setTextClassifier(textClassifier)
    }

    fun getTextClassifier(): TextClassifier? {
        return if (Build.VERSION.SDK_INT >= 26) mEditText.textClassifier else null
    }

    fun onTextContextMenuItem(id: Int): Boolean {
        return mEditText.onTextContextMenuItem(id)
    }

    fun onReceiveContent(payload: ContentInfoCompat): ContentInfoCompat? {
        return (mEditText as TextInputEditText).onReceiveContent(payload)
    }

    fun setKeyListener(keyListener: KeyListener?) {
        mEditText.keyListener = keyListener
    }

    fun setEmojiCompatEnabled(enabled: Boolean) {
        (mEditText as TextInputEditText).isEmojiCompatEnabled = enabled
    }

    fun isEmojiCompatEnabled(): Boolean {
        return (mEditText as TextInputEditText).isEmojiCompatEnabled
    }

    fun getFreezesText(): Boolean {
        return mEditText.freezesText
    }

    fun setText(text: CharSequence?, type: BufferType?) {
        mEditText.setText(text, type)
    }

    fun setSelection(start: Int, stop: Int) {
        mEditText.setSelection(start, stop)
    }

    fun setSelection(index: Int) {
        mEditText.setSelection(index)
    }

    fun selectAll() {
        mEditText.selectAll()
    }

    fun extendSelection(index: Int) {
        mEditText.extendSelection(index)
    }

    fun setAutoSizeTextTypeWithDefaults(autoSizeTextType: Int) {
        if (Build.VERSION.SDK_INT >= 26) mEditText.setAutoSizeTextTypeWithDefaults(autoSizeTextType)
    }

    fun setAutoSizeTextTypeUniformWithConfiguration(
        autoSizeMinTextSize: Int, autoSizeMaxTextSize: Int, autoSizeStepGranularity: Int, unit: Int
    ) {
        if (Build.VERSION.SDK_INT >= 26) mEditText.setAutoSizeTextTypeUniformWithConfiguration(
            autoSizeMinTextSize, autoSizeMaxTextSize, autoSizeStepGranularity, unit
        )
    }

    fun setAutoSizeTextTypeUniformWithPresetSizes(presetSizes: IntArray, unit: Int) {
        if (Build.VERSION.SDK_INT >= 26) mEditText.setAutoSizeTextTypeUniformWithPresetSizes(
            presetSizes,
            unit
        )
    }

    fun getAutoSizeTextType(): Int? {
        return if (Build.VERSION.SDK_INT >= 26) mEditText.autoSizeTextType else null
    }

    fun getAutoSizeStepGranularity(): Int? {
        return if (Build.VERSION.SDK_INT >= 26) mEditText.autoSizeStepGranularity else null
    }

    fun getAutoSizeMinTextSize(): Int? {
        return if (Build.VERSION.SDK_INT >= 26) mEditText.autoSizeMinTextSize else null
    }

    fun getAutoSizeMaxTextSize(): Int? {
        return if (Build.VERSION.SDK_INT >= 26) mEditText.autoSizeMaxTextSize else null
    }

    fun getAutoSizeTextAvailableSizes(): IntArray? {
        return if (Build.VERSION.SDK_INT >= 26) mEditText.autoSizeTextAvailableSizes else null
    }

    fun setTypeface(tf: Typeface?, style: Int) {
        mEditText.setTypeface(tf, style)
    }

    fun length(): Int {
        return mEditText.length()
    }

    fun getEditableText(): Editable? {
        return mEditText.editableText
    }

    fun getLineHeight(): Int {
        return mEditText.lineHeight
    }

    fun getLayout(): Layout {
        return mEditText.layout
    }

    fun getKeyListener(): KeyListener {
        return mEditText.keyListener
    }

    fun getMovementMethod(): MovementMethod {
        return mEditText.movementMethod
    }

    fun setMovementMethod(movement: MovementMethod?) {
        mEditText.movementMethod = movement
    }

    fun getTransformationMethod(): TransformationMethod {
        return mEditText.transformationMethod
    }

    fun setTransformationMethod(method: TransformationMethod?) {
        mEditText.transformationMethod = method
    }

    fun getCompoundPaddingTop(): Int {
        return mEditText.compoundPaddingTop
    }

    fun getCompoundPaddingBottom(): Int {
        return mEditText.compoundPaddingBottom
    }

    fun getCompoundPaddingLeft(): Int {
        return mEditText.compoundPaddingLeft
    }

    fun getCompoundPaddingRight(): Int {
        return mEditText.compoundPaddingRight
    }

    fun getCompoundPaddingStart(): Int {
        return mEditText.compoundPaddingStart
    }

    fun getCompoundPaddingEnd(): Int {
        return mEditText.compoundPaddingEnd
    }

    fun getExtendedPaddingTop(): Int {
        return mEditText.extendedPaddingTop
    }

    fun getExtendedPaddingBottom(): Int {
        return mEditText.extendedPaddingBottom
    }

    fun getTotalPaddingLeft(): Int {
        return mEditText.totalPaddingLeft
    }

    fun getTotalPaddingRight(): Int {
        return mEditText.totalPaddingRight
    }

    fun getTotalPaddingStart(): Int {
        return mEditText.totalPaddingStart
    }

    fun getTotalPaddingEnd(): Int {
        return mEditText.totalPaddingEnd
    }

    fun getTotalPaddingTop(): Int {
        return mEditText.totalPaddingTop
    }

    fun getTotalPaddingBottom(): Int {
        return mEditText.totalPaddingBottom
    }

    fun setCompoundDrawables(
        left: Drawable?, top: Drawable?, right: Drawable?, bottom: Drawable?
    ) {
        mEditText.setCompoundDrawables(left, top, right, bottom)
    }

    fun setCompoundDrawablesWithIntrinsicBounds(left: Int, top: Int, right: Int, bottom: Int) {
        mEditText.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)
    }

    fun setCompoundDrawablesWithIntrinsicBounds(
        left: Drawable?, top: Drawable?, right: Drawable?, bottom: Drawable?
    ) {
        mEditText.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)
    }

    fun setCompoundDrawablesRelative(
        start: Drawable?, top: Drawable?, end: Drawable?, bottom: Drawable?
    ) {
        mEditText.setCompoundDrawablesRelative(start, top, end, bottom)
    }

    fun setCompoundDrawablesRelativeWithIntrinsicBounds(
        start: Int, top: Int, end: Int, bottom: Int
    ) {
        mEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom)
    }

    fun setCompoundDrawablesRelativeWithIntrinsicBounds(
        start: Drawable?, top: Drawable?, end: Drawable?, bottom: Drawable?
    ) {
        mEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom)
    }

    fun getCompoundDrawables(): Array<Drawable?> {
        return mEditText.compoundDrawables
    }

    fun getCompoundDrawablesRelative(): Array<Drawable?> {
        return mEditText.compoundDrawablesRelative
    }

    fun setCompoundDrawablePadding(pad: Int) {
        mEditText.compoundDrawablePadding = pad
    }

    fun getCompoundDrawablePadding(): Int {
        return mEditText.compoundDrawablePadding
    }

    fun setCompoundDrawableTintList(tint: ColorStateList?) {
        if (Build.VERSION.SDK_INT >= 23) TextViewCompat.setCompoundDrawableTintList(mEditText, tint)
    }

    fun getCompoundDrawableTintList(): ColorStateList? {
        return if (Build.VERSION.SDK_INT >= 23) mEditText.compoundDrawableTintList else null
    }

    fun setCompoundDrawableTintMode(tintMode: PorterDuff.Mode?) {
        if (Build.VERSION.SDK_INT >= 23) TextViewCompat.setCompoundDrawableTintMode(
            mEditText,
            tintMode
        )
    }

    fun setCompoundDrawableTintBlendMode(blendMode: BlendMode?) {
        if (Build.VERSION.SDK_INT >= 29) mEditText.compoundDrawableTintBlendMode = blendMode
    }

    fun getCompoundDrawableTintMode(): PorterDuff.Mode? {
        return if (Build.VERSION.SDK_INT >= 23) mEditText.compoundDrawableTintMode else null
    }

    fun getCompoundDrawableTintBlendMode(): BlendMode? {
        return if (Build.VERSION.SDK_INT >= 29) mEditText.compoundDrawableTintBlendMode else null
    }

    fun setEditTextPadding(left: Int, top: Int, right: Int, bottom: Int) {
        mEditText.setPadding(left, top, right, bottom)
    }

    fun setEditTextPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        mEditText.setPaddingRelative(start, top, end, bottom)
    }

    fun setFirstBaselineToTopHeight(firstBaselineToTopHeight: Int) {
        if (Build.VERSION.SDK_INT >= 28) mEditText.firstBaselineToTopHeight =
            firstBaselineToTopHeight
    }

    fun setLastBaselineToBottomHeight(lastBaselineToBottomHeight: Int) {
        if (Build.VERSION.SDK_INT >= 28) mEditText.lastBaselineToBottomHeight =
            lastBaselineToBottomHeight
    }

    fun getFirstBaselineToTopHeight(): Int? {
        return if (Build.VERSION.SDK_INT >= 28) mEditText.firstBaselineToTopHeight else null
    }

    fun getLastBaselineToBottomHeight(): Int? {
        return if (Build.VERSION.SDK_INT >= 28) mEditText.lastBaselineToBottomHeight else null
    }

    fun getAutoLinkMask(): Int {
        return mEditText.autoLinkMask
    }

    fun setTextSelectHandle(textSelectHandle: Drawable) {
        if (Build.VERSION.SDK_INT >= 29) mEditText.setTextSelectHandle(textSelectHandle)
    }

    fun setTextSelectHandle(textSelectHandle: Int) {
        if (Build.VERSION.SDK_INT >= 29) mEditText.setTextSelectHandle(textSelectHandle)
    }

    fun getTextSelectHandle(): Drawable? {
        return if (Build.VERSION.SDK_INT >= 29) mEditText.textSelectHandle else null
    }

    fun setTextSelectHandleLeft(textSelectHandleLeft: Drawable) {
        if (Build.VERSION.SDK_INT >= 29) mEditText.setTextSelectHandleLeft(textSelectHandleLeft)
    }

    fun setTextSelectHandleLeft(textSelectHandleLeft: Int) {
        if (Build.VERSION.SDK_INT >= 29) mEditText.setTextSelectHandleLeft(textSelectHandleLeft)
    }

    fun getTextSelectHandleLeft(): Drawable? {
        return if (Build.VERSION.SDK_INT >= 29) mEditText.textSelectHandleLeft else null
    }

    fun setTextSelectHandleRight(textSelectHandleRight: Drawable) {
        if (Build.VERSION.SDK_INT >= 29) mEditText.setTextSelectHandleRight(textSelectHandleRight)
    }

    fun setTextSelectHandleRight(textSelectHandleRight: Int) {
        if (Build.VERSION.SDK_INT >= 29) mEditText.setTextSelectHandleRight(textSelectHandleRight)
    }

    fun getTextSelectHandleRight(): Drawable? {
        return if (Build.VERSION.SDK_INT >= 29) mEditText.textSelectHandleRight else null
    }

    fun setTextCursorDrawable(textCursorDrawable: Drawable?) {
        if (Build.VERSION.SDK_INT >= 29) mEditText.textCursorDrawable = textCursorDrawable
    }

    fun setTextCursorDrawable(textCursorDrawable: Int) {
        if (Build.VERSION.SDK_INT >= 29) mEditText.setTextCursorDrawable(textCursorDrawable)
    }

    fun getTextCursorDrawable(): Drawable? {
        return if (Build.VERSION.SDK_INT >= 29) mEditText.textCursorDrawable else null
    }

    fun setTextAppearance(resId: Int) {
        if (Build.VERSION.SDK_INT >= 23) mEditText.setTextAppearance(resId)
    }

    fun getTextLocale(): Locale {
        return mEditText.textLocale
    }

    fun getTextLocales(): LocaleList? {
        return if (Build.VERSION.SDK_INT >= 24) mEditText.textLocales else null
    }

    fun setTextLocale(locale: Locale) {
        mEditText.textLocale = locale
    }

    fun setTextLocales(locales: LocaleList) {
        if (Build.VERSION.SDK_INT >= 24) mEditText.textLocales = locales
    }

    @ExportedProperty(category = "text")
    fun getTextSize(): Float {
        return mEditText.textSize
    }

    fun setTextSize(size: Float) {
        mEditText.textSize = size
    }

    fun setTextSize(unit: Int, size: Float) {
        mEditText.setTextSize(unit, size)
    }

    fun getTextSizeUnit(): Int? {
        return if (Build.VERSION.SDK_INT >= 30) mEditText.textSizeUnit else null
    }

    fun getTextScaleX(): Float {
        return mEditText.textScaleX
    }

    fun setTextScaleX(size: Float) {
        mEditText.textScaleX = size
    }

    fun setElegantTextHeight(elegant: Boolean) {
        mEditText.isElegantTextHeight = elegant
    }

    fun setFallbackLineSpacing(enabled: Boolean) {
        if (Build.VERSION.SDK_INT >= 28) mEditText.isFallbackLineSpacing = enabled
    }

    fun isFallbackLineSpacing(): Boolean? {
        return if (Build.VERSION.SDK_INT >= 28) mEditText.isFallbackLineSpacing else null
    }

    fun isElegantTextHeight(): Boolean? {
        return if (Build.VERSION.SDK_INT >= 28) mEditText.isElegantTextHeight else null
    }

    fun getLetterSpacing(): Float {
        return mEditText.letterSpacing
    }

    fun setLetterSpacing(letterSpacing: Float) {
        mEditText.letterSpacing = letterSpacing
    }

    fun getFontFeatureSettings(): String? {
        return mEditText.fontFeatureSettings
    }

    fun getFontVariationSettings(): String? {
        return if (Build.VERSION.SDK_INT >= 26) mEditText.fontVariationSettings else null
    }

    fun setBreakStrategy(breakStrategy: Int) {
        if (Build.VERSION.SDK_INT >= 23) mEditText.breakStrategy = breakStrategy
    }

    fun getBreakStrategy(): Int? {
        return if (Build.VERSION.SDK_INT >= 23) mEditText.breakStrategy else null
    }

    fun setHyphenationFrequency(hyphenationFrequency: Int) {
        if (Build.VERSION.SDK_INT >= 23) mEditText.hyphenationFrequency = hyphenationFrequency
    }

    fun getHyphenationFrequency(): Int? {
        return if (Build.VERSION.SDK_INT >= 23) mEditText.hyphenationFrequency else null
    }

    fun getTextMetricsParams(): PrecomputedText.Params? {
        return if (Build.VERSION.SDK_INT >= 28) mEditText.textMetricsParams else null
    }

    fun setTextMetricsParams(params: PrecomputedText.Params) {
        if (Build.VERSION.SDK_INT >= 28) mEditText.textMetricsParams = params
    }

    fun setJustificationMode(justificationMode: Int) {
        if (Build.VERSION.SDK_INT >= 26) mEditText.justificationMode = justificationMode
    }

    fun getJustificationMode(): Int? {
        return if (Build.VERSION.SDK_INT >= 26) mEditText.justificationMode else null
    }

    fun setFontFeatureSettings(fontFeatureSettings: String?) {
        mEditText.fontFeatureSettings = fontFeatureSettings
    }

    fun setFontVariationSettings(fontVariationSettings: String?): Boolean? {
        return if (Build.VERSION.SDK_INT >= 26) mEditText.setFontVariationSettings(
            fontVariationSettings
        ) else null
    }

    fun setTextColor(color: Int) {
        mEditText.setTextColor(color)
    }

    fun setTextColor(colors: ColorStateList?) {
        mEditText.setTextColor(colors)
    }

    fun getTextColors(): ColorStateList {
        return mEditText.textColors
    }

    fun getCurrentTextColor(): Int {
        return mEditText.currentTextColor
    }

    fun setHighlightColor(color: Int) {
        mEditText.highlightColor = color
    }

    fun getHighlightColor(): Int {
        return mEditText.highlightColor
    }

    fun setShowSoftInputOnFocus(show: Boolean) {
        mEditText.showSoftInputOnFocus = show
    }

    fun getShowSoftInputOnFocus(): Boolean {
        return mEditText.showSoftInputOnFocus
    }

    fun setShadowLayer(radius: Float, dx: Float, dy: Float, color: Int) {
        mEditText.setShadowLayer(radius, dx, dy, color)
    }

    fun getShadowRadius(): Float {
        return mEditText.shadowRadius
    }

    fun getShadowDx(): Float {
        return mEditText.shadowDx
    }

    fun getShadowDy(): Float {
        return mEditText.shadowDy
    }

    fun getShadowColor(): Int {
        return mEditText.shadowColor
    }

    fun getPaint(): TextPaint? {
        return mEditText.paint
    }

    fun setAutoLinkMask(mask: Int) {
        mEditText.autoLinkMask = mask
    }

    fun setLinksClickable(whether: Boolean) {
        mEditText.linksClickable = whether
    }

    fun getLinksClickable(): Boolean {
        return mEditText.linksClickable
    }

    fun getUrls(): Array<URLSpan?>? {
        return mEditText.urls
    }

    fun setHintTextColor(color: Int) {
        mEditText.setHintTextColor(color)
    }

    fun getHintTextColors(): ColorStateList {
        return mEditText.hintTextColors
    }

    fun getCurrentHintTextColor(): Int {
        return mEditText.currentHintTextColor
    }

    fun setLinkTextColor(color: Int) {
        mEditText.setLinkTextColor(color)
    }

    fun setLinkTextColor(colors: ColorStateList?) {
        mEditText.setLinkTextColor(colors)
    }

    fun getLinkTextColors(): ColorStateList {
        return mEditText.linkTextColors
    }

    fun getPaintFlags(): Int {
        return mEditText.paintFlags
    }

    fun setPaintFlags(flags: Int) {
        mEditText.paintFlags = flags
    }

    fun setHorizontallyScrolling(whether: Boolean) {
        mEditText.setHorizontallyScrolling(whether)
    }

    fun isHorizontallyScrollable(): Boolean? {
        return if (Build.VERSION.SDK_INT >= 29) mEditText.isHorizontallyScrollable else null
    }

    fun setMinLines(minLines: Int) {
        mEditText.minLines = minLines
    }

    fun getMinLines(): Int {
        return mEditText.minLines
    }

    fun setMinHeight(minPixels: Int) {
        mEditText.minHeight = minPixels
    }

    fun getMinHeight(): Int {
        return mEditText.minHeight
    }

    fun setMaxLines(maxLines: Int) {
        mEditText.maxLines = maxLines
    }

    fun getMaxLines(): Int {
        return mEditText.maxLines
    }

    fun setMaxHeight(maxPixels: Int) {
        mEditText.maxHeight = maxPixels
    }

    fun getMaxHeight(): Int {
        return mEditText.maxHeight
    }

    fun setLines(lines: Int) {
        mEditText.setLines(lines)
    }

    fun setHeight(pixels: Int) {
        mEditText.height = pixels
    }

    fun setEms(ems: Int) {
        mEditText.setEms(ems)
    }

    fun setWidth(pixels: Int) {
        mEditText.width = pixels
    }

    fun setLineSpacing(add: Float, multi: Float) {
        mEditText.setLineSpacing(add, multi)
    }

    fun getLineSpacingMultiplier(): Float {
        return mEditText.lineSpacingMultiplier
    }

    fun getLineSpacingExtra(): Float {
        return mEditText.lineSpacingExtra
    }

    fun setLineHeight(lineHeight: Int) {
        if (Build.VERSION.SDK_INT >= 28) mEditText.lineHeight = lineHeight
    }

    fun append(text: CharSequence?) {
        mEditText.append(text)
    }

    fun append(text: CharSequence?, start: Int, end: Int) {
        mEditText.append(text, start, end)
    }

    fun setFreezesText(freezesText: Boolean) {
        mEditText.freezesText = freezesText
    }

    fun setEditableFactory(factory: Editable.Factory?) {
        mEditText.setEditableFactory(factory)
    }

    fun setSpannableFactory(factory: Spannable.Factory?) {
        mEditText.setSpannableFactory(factory)
    }

    fun setText(text: CharSequence?) {
        mEditText.setText(text)
    }

    fun setTextKeepState(text: CharSequence?) {
        mEditText.setTextKeepState(text)
    }

    fun setText(text: CharArray, start: Int, len: Int) {
        mEditText.setText(text, start, len)
    }

    fun setTextKeepState(text: CharSequence?, type: BufferType?) {
        mEditText.setTextKeepState(text, type)
    }

    fun setText(resId: Int) {
        mEditText.setText(resId)
    }

    fun setText(resId: Int, type: BufferType?) {
        mEditText.setText(resId, type)
    }

    fun isSingleLine(): Boolean? {
        return if (Build.VERSION.SDK_INT >= 29) mEditText.isSingleLine else null
    }

    fun setInputType(type: Int) {
        mEditText.inputType = type
    }

    fun setRawInputType(type: Int) {
        mEditText.setRawInputType(type)
    }

    fun getInputType(): Int {
        return mEditText.inputType
    }

    fun setImeOptions(imeOptions: Int) {
        mEditText.imeOptions = imeOptions
    }

    fun getImeOptions(): Int {
        return mEditText.imeOptions
    }

    fun setImeActionLabel(label: CharSequence?, actionId: Int) {
        mEditText.setImeActionLabel(label, actionId)
    }

    fun getImeActionLabel(): CharSequence? {
        return mEditText.imeActionLabel
    }

    fun getImeActionId(): Int {
        return mEditText.imeActionId
    }

    fun setOnEditorActionListener(l: TextView.OnEditorActionListener?) {
        mEditText.setOnEditorActionListener(l)
    }

    fun onEditorAction(actionCode: Int) {
        mEditText.onEditorAction(actionCode)
    }

    fun setPrivateImeOptions(type: String?) {
        mEditText.privateImeOptions = type
    }

    fun getPrivateImeOptions(): String? {
        return mEditText.privateImeOptions
    }

    @Throws(IOException::class, XmlPullParserException::class)
    fun setInputExtras(xmlResId: Int) {
        mEditText.setInputExtras(xmlResId)
    }

    fun getInputExtras(create: Boolean): Bundle? {
        return mEditText.getInputExtras(create)
    }

    fun setImeHintLocales(hintLocales: LocaleList?) {
        if (Build.VERSION.SDK_INT >= 24) mEditText.imeHintLocales = hintLocales
    }

    fun getImeHintLocales(): LocaleList? {
        return if (Build.VERSION.SDK_INT >= 24) mEditText.imeHintLocales else null
    }

    fun setError(error: CharSequence?, icon: Drawable?) {
        mEditText.setError(error, icon)
    }

    fun setFilters(filters: Array<InputFilter?>?) {
        mEditText.filters = filters
    }

    fun getFilters(): Array<InputFilter?>? {
        return mEditText.filters
    }

    fun onPreDraw(): Boolean {
        return mEditText.onPreDraw()
    }

    fun isTextSelectable(): Boolean {
        return mEditText.isTextSelectable
    }

    fun setTextIsSelectable(selectable: Boolean) {
        mEditText.setTextIsSelectable(selectable)
    }

    fun getLineCount(): Int {
        return mEditText.lineCount
    }

    fun getLineBounds(line: Int, bounds: Rect?): Int {
        return mEditText.getLineBounds(line, bounds)
    }

    fun extractText(request: ExtractedTextRequest?, outText: ExtractedText?): Boolean {
        return mEditText.extractText(request, outText)
    }

    fun setExtractedText(text: ExtractedText?) {
        mEditText.setExtractedText(text)
    }

    fun onCommitCorrection(info: CorrectionInfo?) {
        mEditText.onCommitCorrection(info)
    }

    fun beginBatchEdit() {
        mEditText.beginBatchEdit()
    }

    fun endBatchEdit() {
        mEditText.endBatchEdit()
    }

    fun onBeginBatchEdit() {
        mEditText.onBeginBatchEdit()
    }

    fun onEndBatchEdit() {
        mEditText.onEndBatchEdit()
    }

    fun onPrivateIMECommand(action: String?, data: Bundle?): Boolean {
        return mEditText.onPrivateIMECommand(action, data)
    }

    fun setIncludeFontPadding(includePad: Boolean) {
        mEditText.includeFontPadding = includePad
    }

    fun getIncludeFontPadding(): Boolean {
        return mEditText.includeFontPadding
    }

    fun bringPointIntoView(offset: Int): Boolean {
        return mEditText.bringPointIntoView(offset)
    }

    fun moveCursorToVisibleOffset(): Boolean {
        return mEditText.moveCursorToVisibleOffset()
    }

    @ExportedProperty(category = "text")
    fun getSelectionStart(): Int {
        return mEditText.selectionStart
    }

    @ExportedProperty(category = "text")
    fun getSelectionEnd(): Int {
        return mEditText.selectionEnd
    }

    fun hasSelection(): Boolean {
        return mEditText.hasSelection()
    }

    fun setSingleLine() {
        mEditText.setSingleLine()
    }

    fun setAllCaps(allCaps: Boolean) {
        mEditText.isAllCaps = allCaps
    }

    fun isAllCaps(): Boolean? {
        return if (Build.VERSION.SDK_INT >= 28) mEditText.isAllCaps else null
    }

    fun setSingleLine(singleLine: Boolean) {
        mEditText.isSingleLine = singleLine
    }

    fun setEllipsize(where: TextUtils.TruncateAt?) {
        mEditText.ellipsize = where
    }

    fun setMarqueeRepeatLimit(marqueeLimit: Int) {
        mEditText.marqueeRepeatLimit = marqueeLimit
    }

    fun getMarqueeRepeatLimit(): Int {
        return mEditText.marqueeRepeatLimit
    }

    @ExportedProperty
    fun getEllipsize(): TextUtils.TruncateAt? {
        return mEditText.ellipsize
    }

    fun setSelectAllOnFocus(selectAllOnFocus: Boolean) {
        mEditText.setSelectAllOnFocus(selectAllOnFocus)
    }

    fun setCursorVisible(visible: Boolean) {
        mEditText.isCursorVisible = visible
    }

    fun isCursorVisible(): Boolean {
        return mEditText.isCursorVisible
    }

    fun addTextChangedListener(watcher: TextWatcher?) {
        mEditText.addTextChangedListener(watcher)
    }

    fun removeTextChangedListener(watcher: TextWatcher?) {
        mEditText.removeTextChangedListener(watcher)
    }

    fun clearComposingText() {
        mEditText.clearComposingText()
    }

    fun didTouchFocusSelect(): Boolean {
        return mEditText.didTouchFocusSelect()
    }

    fun setScroller(s: Scroller?) {
        mEditText.setScroller(s)
    }

    fun isInputMethodTarget(): Boolean {
        return mEditText.isInputMethodTarget
    }

    fun isSuggestionsEnabled(): Boolean {
        return mEditText.isSuggestionsEnabled
    }

    fun setCustomInsertionActionModeCallback(actionModeCallback: ActionMode.Callback?) {
        if (Build.VERSION.SDK_INT >= 23) mEditText.customInsertionActionModeCallback =
            actionModeCallback
    }

    fun getCustomInsertionActionModeCallback(): ActionMode.Callback? {
        return if (Build.VERSION.SDK_INT >= 23) mEditText.customInsertionActionModeCallback else null
    }

    fun getOffsetForPosition(x: Float, y: Float): Int {
        return mEditText.getOffsetForPosition(x, y)
    }

    fun getTextDirectionHeuristic(): TextDirectionHeuristic? {
        return if (Build.VERSION.SDK_INT >= 29) mEditText.textDirectionHeuristic else null
    }

    fun showDropDown() {
        (editText as MaterialAutoCompleteTextView).showDropDown()
    }

    fun <T> setAdapter(adapter: T?) where T : ListAdapter?, T : Filterable? {
        return (editText as MaterialAutoCompleteTextView).setAdapter(adapter)
    }

    fun setDropDownBackgroundResource(@DrawableRes resId: Int) {
        (editText as MaterialAutoCompleteTextView).setDropDownBackgroundResource(resId)
    }

    fun setAutoCompleteEmojiCompatEnabled(enabled: Boolean) {
        (editText as MaterialAutoCompleteTextView).isEmojiCompatEnabled = enabled
    }

    fun isAutoCompleteEmojiCompatEnabled(): Boolean {
        return (editText as MaterialAutoCompleteTextView).isEmojiCompatEnabled
    }

    fun setCompletionHint(hint: CharSequence?) {
        (editText as MaterialAutoCompleteTextView).completionHint = hint
    }

    fun getCompletionHint(): CharSequence? {
        return (editText as MaterialAutoCompleteTextView).completionHint
    }

    fun getDropDownWidth(): Int {
        return (editText as MaterialAutoCompleteTextView).dropDownWidth
    }

    fun setDropDownWidth(width: Int) {
        (editText as MaterialAutoCompleteTextView).dropDownWidth = width
    }

    fun getDropDownHeight(): Int {
        return (editText as MaterialAutoCompleteTextView).dropDownHeight
    }

    fun setDropDownHeight(height: Int) {
        (editText as MaterialAutoCompleteTextView).dropDownHeight = height
    }

    fun getDropDownAnchor(): Int {
        return (editText as MaterialAutoCompleteTextView).dropDownAnchor
    }

    fun setDropDownAnchor(id: Int) {
        (editText as MaterialAutoCompleteTextView).dropDownAnchor = id
    }

    fun getDropDownBackground(): Drawable? {
        return (editText as MaterialAutoCompleteTextView).dropDownBackground
    }

    fun setDropDownBackgroundDrawable(d: Drawable?) {
        (editText as MaterialAutoCompleteTextView).setDropDownBackgroundDrawable(d)
    }

    fun setDropDownVerticalOffset(offset: Int) {
        (editText as MaterialAutoCompleteTextView).dropDownVerticalOffset = offset
    }

    fun getDropDownVerticalOffset(): Int {
        return (editText as MaterialAutoCompleteTextView).dropDownVerticalOffset
    }

    fun setDropDownHorizontalOffset(offset: Int) {
        (editText as MaterialAutoCompleteTextView).dropDownHorizontalOffset = offset
    }

    fun getDropDownHorizontalOffset(): Int {
        return (editText as MaterialAutoCompleteTextView).dropDownHorizontalOffset
    }

    fun getThreshold(): Int {
        return (editText as MaterialAutoCompleteTextView).threshold
    }

    fun setThreshold(threshold: Int) {
        (editText as MaterialAutoCompleteTextView).threshold = threshold
    }

    fun setOnItemClickListener(l: OnItemClickListener?) {
        (editText as MaterialAutoCompleteTextView).onItemClickListener = l
    }

    fun setOnItemSelectedListener(l: AdapterView.OnItemSelectedListener?) {
        (editText as MaterialAutoCompleteTextView).onItemSelectedListener = l
    }

    fun getOnItemClickListener(): OnItemClickListener? {
        return (editText as MaterialAutoCompleteTextView).onItemClickListener
    }

    fun getOnItemSelectedListener(): AdapterView.OnItemSelectedListener? {
        return (editText as MaterialAutoCompleteTextView).onItemSelectedListener
    }

    fun setOnDismissListener(dismissListener: AutoCompleteTextView.OnDismissListener?) {
        (editText as MaterialAutoCompleteTextView).setOnDismissListener(dismissListener)
    }

    fun getAdapter(): ListAdapter? {
        return (editText as MaterialAutoCompleteTextView).adapter
    }

    fun enoughToFilter(): Boolean {
        return (editText as MaterialAutoCompleteTextView).enoughToFilter()
    }

    fun refreshAutoCompleteResults() {
        if (Build.VERSION.SDK_INT >= 29) (editText as MaterialAutoCompleteTextView).refreshAutoCompleteResults()
    }

    fun isPopupShowing(): Boolean {
        return (editText as MaterialAutoCompleteTextView).isPopupShowing
    }

    fun clearListSelection() {
        (editText as MaterialAutoCompleteTextView).clearListSelection()
    }

    fun setListSelection(position: Int) {
        (editText as MaterialAutoCompleteTextView).listSelection = position
    }

    fun getListSelection(): Int {
        return (editText as MaterialAutoCompleteTextView).listSelection
    }

    fun performCompletion() {
        (editText as MaterialAutoCompleteTextView).performCompletion()
    }

    fun onCommitCompletion(completion: CompletionInfo?) {
        (editText as MaterialAutoCompleteTextView).onCommitCompletion(completion)
    }

    fun isPerformingCompletion(): Boolean {
        return (editText as MaterialAutoCompleteTextView).isPerformingCompletion
    }

    fun setText(text: CharSequence?, filter: Boolean) {
        (editText as MaterialAutoCompleteTextView).setText(text, filter)
    }

    fun onFilterComplete(count: Int) {
        (editText as MaterialAutoCompleteTextView).onFilterComplete(count)
    }

    fun dismissDropDown() {
        (editText as MaterialAutoCompleteTextView).dismissDropDown()
    }

    fun getInputMethodMode(): Int? {
        return if (Build.VERSION.SDK_INT >= 29) (editText as MaterialAutoCompleteTextView).inputMethodMode else null
    }

    fun setInputMethodMode(mode: Int) {
        if (Build.VERSION.SDK_INT >= 29) (editText as MaterialAutoCompleteTextView).inputMethodMode =
            mode
    }

    fun setValidator(validator: AutoCompleteTextView.Validator?) {
        (editText as MaterialAutoCompleteTextView).validator = validator
    }

    fun getValidator(): AutoCompleteTextView.Validator? {
        return (editText as MaterialAutoCompleteTextView).validator
    }

    fun performValidation() {
        (editText as MaterialAutoCompleteTextView).performValidation()
    }
}