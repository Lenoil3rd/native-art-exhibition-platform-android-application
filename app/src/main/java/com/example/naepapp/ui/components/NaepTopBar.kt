package com.example.naepapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.naepapp.R
import com.example.naepapp.ui.theme.BrightSnow
import com.example.naepapp.ui.theme.Onyx
import com.example.naepapp.ui.model.CategoryUi
// If CategoryUi is in a different package, import it here, e.g.:
// import com.example.naepapp.data.models.CategoryUi

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

// COMPONENT: CategoryDropdown — pick one of the five art categories (Open a gallery, Add Creation)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(categories: List<CategoryUi>, selectedId: Int?, label: String, onSelect: (Int) -> Unit, error: String? = null) {
    var open by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(open, { open = it }) {
        OutlinedTextField(
            value = categories.firstOrNull { it.id == selectedId }?.name.orEmpty(), onValueChange = {}, readOnly = true,
            label = { Text(label) }, isError = error != null, supportingText = error?.let { { Text(it) } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(open) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth())
        ExposedDropdownMenu(open, { open = false }) {
            categories.forEach { c -> DropdownMenuItem(text = { Text(c.name) }, onClick = { onSelect(c.id); open = false }) }
        }
    }
}