package com.example.naepapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.naepapp.R
import com.example.naepapp.core.common.MalawiDistricts
import com.example.naepapp.ui.theme.BlackForest
import com.example.naepapp.ui.theme.BrightSnow
import com.example.naepapp.ui.theme.DustGrey
import com.example.naepapp.ui.theme.Onyx
import com.example.naepapp.ui.theme.OxBlood
import com.example.naepapp.ui.model.CategoryUi

// COMPONENT: NaepTopBar — centered title, optional back arrow
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NaepTopBar(title: String, onBack: (() -> Unit)? = null) {
    CenterAlignedTopAppBar(
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = { if (onBack != null) IconButton(onBack) { Icon(painterResource(R.drawable.ic_arrow_back), "Back") } },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Onyx,
            scrolledContainerColor = Onyx,
            navigationIconContentColor = BrightSnow,
            titleContentColor = BrightSnow,
            actionIconContentColor = BrightSnow,
        ),
    )
}

// COMPONENT: SectionHeader — bold section title
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) =
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = modifier.padding(vertical = 8.dp))

// COMPONENT: NaepDropdown — reusable dropdown component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NaepDropdown(
    options: List<String>,
    selected: String?,
    label: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    val focusManager = LocalFocusManager.current
    var open by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = open,
        onExpandedChange = { newOpen ->
            open = newOpen
            if (newOpen) {
                focusManager.clearFocus()
            }
        },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(open) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BlackForest, unfocusedBorderColor = DustGrey, errorBorderColor = OxBlood,
                focusedLabelColor = BlackForest, cursorColor = BlackForest
            ),
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth().bringIntoViewOnFocus()
        )
        ExposedDropdownMenu(
            expanded = open,
            onDismissRequest = { open = false },
            modifier = Modifier.heightIn(max = 280.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        open = false
                    }
                )
            }
        }
    }
}

// COMPONENT: CategoryDropdown — pick one of the five art categories (Open a gallery, Add Creation)
@Composable
fun CategoryDropdown(categories: List<CategoryUi>, selectedId: Int?, label: String, onSelect: (Int) -> Unit, error: String? = null) {
    val selectedName = categories.firstOrNull { it.id == selectedId }?.name
    NaepDropdown(
        options = categories.map { it.name },
        selected = selectedName,
        label = label,
        onSelect = { selectedNameStr ->
            categories.firstOrNull { it.name == selectedNameStr }?.id?.let { onSelect(it) }
        },
        error = error
    )
}

// COMPONENT: DistrictDropdown — pick a district of Malawi
@Composable
fun DistrictDropdown(selected: String?, onSelect: (String) -> Unit, error: String? = null) {
    NaepDropdown(
        options = MalawiDistricts.all,
        selected = selected,
        label = stringResource(R.string.location),
        onSelect = onSelect,
        error = error
    )
}

@Preview(showBackground = true)
@Composable
fun DistrictDropdownPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            DistrictDropdown(selected = null, onSelect = {})
            Spacer(modifier = Modifier.height(16.dp))
            DistrictDropdown(selected = "Zomba", onSelect = {})
        }
    }
}