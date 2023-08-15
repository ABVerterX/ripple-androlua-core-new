require("import")
import "android.app.AlertDialog"
onError = function(error, e)
    print(e.class)
    AlertDialog.Builder(this)
               .setTitle(error)
               .setMessage(e.message)
               .show()
    return true
end

import "vinx.material.preference.widget.*"
import "vinx.material.preference.widget.SwitchPreference"
import "androidx.appcompat.widget.LinearLayoutCompat"
import "android.graphics.drawable.BitmapDrawable"
import "com.google.android.material.divider.MaterialDivider"
import "android.widget.ScrollView"
import "vinx.material.preference.datastore.SharedPreferencesDataStore"
import "vinx.material.preference.*"
import "vinx.material.button.IconButton"
--import "java.lang.reflect.Array"

this.contentView = loadlayout {
    ScrollView,
    layout_width = "match",
    layout_height = "match",
    {
        LinearLayoutCompat,
        layout_width = "match",
        layout_height = "match",
        orientation = "vertical",
        {
            IconButton,
            icon = BitmapDrawable(loadbitmap"icon.png")
        },
        {
            SubHeaderPreference,
            layout_width = "match",
            layout_height = "wrap",
            title = "SubHeaderPreference",
        },
        {
            Preference,
            layout_width = "match",
            layout_height = "wrap",
            title = "Preference",
        },
        {
            Preference,
            layout_width = "match",
            layout_height = "wrap",
            title = "Preference",
            summary = "Lorem ipsum dolor.",
        },
        {
            MaterialDivider
        },
        {
            SubHeaderPreference,
            layout_width = "match",
            layout_height = "wrap",
            title = "SubHeaderPreference",
            blankEnabled = true
        },
        {
            Preference,
            layout_width = "match",
            layout_height = "wrap",
            title = "Preference: BlankIcon",
            summary = "Lorem ipsum dolor.",
            blankIcon = true,
        },
        {
            Preference,
            layout_width = "match",
            layout_height = "wrap",
            title = "Preference",
            summary = "Lorem ipsum dolor.",
            src = "icon.png"
        },
        {
            EndIconPreference,
            layout_width = "match",
            layout_height = "wrap",
            title = "EndIconPreference",
            summary = "Lorem ipsum dolor.",
            src = "icon.png",
            endIcon = BitmapDrawable(loadbitmap "icon.png"),
        },
        {
            MultiActionPreference,
            layout_width = "match",
            layout_height = "wrap",
            title = "MultiActionPreference",
            summary = "Lorem ipsum dolor.",
            src = "icon.png",
            actionIcon = BitmapDrawable(loadbitmap "icon.png"),
        },
        {
            SwitchPreference,
            id = "mSwitchPreference",
            layout_width = "match",
            layout_height = "wrap",
            title = "SwitchPreference",
            summary = "Lorem ipsum dolor.",
            src = "icon.png",
            material3StyleEnabled = true,
        },
        {
            SwitchPreference,
            id = "mSwitchPreference2",
            layout_width = "match",
            layout_height = "wrap",
            title = "SwitchPreference",
            summary = "Lorem ipsum dolor.",
            src = "icon.png",
        },
        {
            SliderPreference,
            layout_width = "match",
            layout_height = "wrap",
            title = "SliderPreference",
            summary = "Lorem ipsum dolor.",
            src = "icon.png",
        },
        {
            CheckBoxPreference,
            layout_width = "match",
            layout_height = "wrap",
            title = "CheckBoxPreference",
            summary = "Lorem ipsum dolor.",
            src = "icon.png"
        },
        {
            EditTextPreference,
            id = "mEditTextPreference",
            layout_width = "match",
            layout_height = "wrap",
            title = "EditTextPreference",
            summary = "Lorem ipsum dolor.",
            src = "icon.png"
        },
        {
            TextFieldPreference,
            id = "mTextFieldPreference",
            layout_width = "match",
            layout_height = "wrap",
            title = "TextFieldPreference",
            summary = "Lorem ipsum dolor.",
            src = "icon.png"
        },
        {
            EndTextPreference,
            layout_width = "match",
            layout_height = "wrap",
            title = "EndTextPreference",
            summary = "Lorem ipsum dolor.",
            src = "icon.png",
            endText = "100%"
        },
        {
            ListPreference,
            id = "l",
            layout_width = "match",
            layout_height = "wrap",
            title = "ListPreference",
            summary = "Lorem ipsum dolor.",
            src = "icon.png",
        },
        {
            MenuPreference,
            id = "m",
            layout_width = "match",
            layout_height = "wrap",
            title = "MenuPreference",
            summary = "Lorem ipsum dolor.",
            src = "icon.png",
        },
        {
            SingleChoicePreference,
            id = "a",
            layout_width = "match",
            layout_height = "wrap",
            title = "SingleChoicePreference",
            summary = "Lorem ipsum dolor.",
            src = "icon.png",
        },
        {
            MultiChoicePreference,
            id = "g",
            layout_width = "match",
            layout_height = "wrap",
            title = "MultiChoicePreference",
            summary = "Lorem ipsum dolor.",
            src = "icon.png",
        },
        {
            ProgressPreference,
            layout_width = "match",
            layout_height = "wrap",
            title = "ProgressPreference",
            summary = "Lorem ipsum dolor.",
            src = "icon.png",
        },
        {
            MaterialDivider
        },
        {
            TipPreference,
            layout_width = "match",
            layout_height = "wrap",
            summary = "TipPreference\nLorem ipsum dolor.\nLorem ipsum dolor.",
            src = "icon.png",
        },
    }

}

l.items = { "Apple", "Banana", "Orange" }
a.items = { "Apple", "Banana", "Orange" }
g.items = { "Apple", "Banana", "Orange" }
m.items = { "Apple", "Banana", "Orange" }

local manager = PreferenceManager(SharedPreferencesDataStore())

mSwitchPreference.key = "test"
mSwitchPreference.manager = manager
mSwitchPreference.update()

mSwitchPreference2.key = "test2"
mSwitchPreference2.manager = manager
mSwitchPreference2.update()

mEditTextPreference.key = "test3"
mEditTextPreference.manager = manager
mEditTextPreference.update()

mTextFieldPreference.key = "test4"
mTextFieldPreference.manager = manager
mTextFieldPreference.update()

a.key = "a"
a.manager = manager
a.update()

g.key = "g"
g.manager = manager
g.update()

m.key = "mmmmmmmmm"
m.manager = manager
m.update()
--test.material3StyleEnabled = true
--this.window.decorView.setOnApplyWindowInsetsListener(function()
--
--end)
