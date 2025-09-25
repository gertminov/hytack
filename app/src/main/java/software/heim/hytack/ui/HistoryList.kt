package software.heim.hytack.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import androidx.room.util.TableInfo
import software.heim.hytack.data.database.Intake
import software.heim.hytack.data.domain.mapper
import software.heim.hytack.data.domain.milliliter
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HistoryList(
    pagedIntakes: LazyPagingItems<TimelineItem>,
    onDeleteIntake: (Intake) -> Unit,
    visible: Boolean,
    modifier: Modifier = Modifier
) {


    val listState = rememberLazyListState() // Create and remember the LazyListState

    LaunchedEffect(pagedIntakes.itemCount) {
        if (pagedIntakes.itemCount > 0) {
            if (!visible) {
                listState.animateScrollToItem(index = 0)
            }
        }
    }
    Column(
        modifier = modifier,
    ) {
//        Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
//            IconButton({}) {
//                Icon(Icons.Outlined.Share, "Share")
//            }
//        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = true,
            state = listState
        ) {
            items(
                pagedIntakes.itemCount,
                key = pagedIntakes.itemKey { it.id }
            ) { idx ->
                val intake = pagedIntakes[idx]
                intake?.let {
                    when (it) {
                        is TimelineItem.IntakeItem -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    it.intake.amountMl.mapper().milliliter().format(),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                IconButton({ onDeleteIntake(it.intake) }) {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        "delete",
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }

                        is TimelineItem.DaySeperator -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    it.dateTString,
                                    color = MaterialTheme.colorScheme.secondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }


}