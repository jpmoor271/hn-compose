package com.example.hncompose

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.remember
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.layout.Column
import androidx.ui.layout.fillMaxWidth
import androidx.ui.layout.padding
import androidx.ui.material.*
import androidx.ui.material.ripple.ripple
import androidx.ui.text.TextStyle
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import androidx.ui.unit.sp
import com.example.hackernetwork.HNItem
import com.example.hackernetwork.HackerNewsRepo
import com.example.hackernetwork.HackerNewsRetrofit
import com.example.hncompose.util.createWithFactory
import com.example.hncompose.util.shortUrlString
import com.example.hncompose.viewmodel.HackerNewsViewModel

class MainActivity : AppCompatActivity() {

    private val topStoriesViewModel: HackerNewsViewModel by viewModels {
        createWithFactory {
            HackerNewsViewModel(
                repo = HackerNewsRepo(
                    HackerNewsRetrofit.retrofitInstance
                )
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TopNewsScreen(
                    appStatus = AppDataStatus,
                    loadMoreTopStoriesClicked = {
                        topStoriesViewModel.getNextTopNewsChunk()
                    }
                )
            }
        }

        topStoriesViewModel.getTopStories()
    }
}

@Composable
fun TopNewsScreen(
    appStatus: AppDataStatusHolder,
    loadMoreTopStoriesClicked: () -> Unit,
    scaffoldState: ScaffoldState = remember { ScaffoldState() }
) {
    Scaffold(
            scaffoldState = scaffoldState,
            topAppBar = {
                TopAppBar(
                        title = { Text(text = "HN Compose") }
                )
            },
            bodyContent = {
                TopNewsScreenBody(
                    stories = appStatus.topStories,
                    loadMoreTopStoriesClicked = loadMoreTopStoriesClicked
                )
            }
    )
}

@Composable
fun TopNewsScreenBody(
        stories: List<HNItem>,
        loadMoreTopStoriesClicked: () -> Unit) {
    VerticalScroller {
        Column {
            stories.forEach { story ->
                StoryCard(story = story)
            }
            if (stories.isNotEmpty()) {
                LoadMoreCard(loadMoreTopStoriesClicked = loadMoreTopStoriesClicked)
            }
        }
    }
}

@Composable
fun StoryCard(story: HNItem) {
    Card(
            shape = RoundedCornerShape(size = 6.dp),
            elevation = 4.dp,
            modifier = Modifier
                    .padding(
                            start = 8.dp,
                            end = 8.dp,
                            top = 4.dp,
                            bottom = 4.dp
                    )
                    .fillMaxWidth()
    ) {
        Clickable(
                modifier = Modifier.ripple(),
                onClick = {
                    println("Story: ${story.title}")
                }
        ) {
            Column {
                Text(
                    text = story.title ?: "",
                    modifier = Modifier
                        .padding(
                            start = 8.dp,
                            end = 8.dp,
                            top = 8.dp,
                            bottom = 4.dp
                        )
                )
                Text(
                    text = story.url.shortUrlString,
                    modifier = Modifier
                        .padding(
                            top = 0.dp,
                            bottom = 8.dp,
                            start = 8.dp,
                            end = 8.dp
                        ),
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                )
            }
        }
    }
}

@Composable
fun LoadMoreCard(loadMoreTopStoriesClicked: () -> Unit) {
    Card(
        shape = RoundedCornerShape(size = 0.dp),
        elevation = 0.dp,
        modifier = Modifier
            .padding(
                start = 8.dp,
                end = 8.dp,
                top = 4.dp,
                bottom = 4.dp
            )
            .fillMaxWidth()
    ) {
        Button(
            text = {
                Text(text = "Load More")
            },
            onClick = loadMoreTopStoriesClicked
        )
    }
}

@Preview
@Composable
fun DefaultPreview() {
    MaterialTheme {
        TopNewsScreen(MockAppDataStatus, {})
    }
}