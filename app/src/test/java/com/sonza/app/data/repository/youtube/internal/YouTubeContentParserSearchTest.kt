package com.sonza.app.data.repository.youtube.internal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class YouTubeContentParserSearchTest {

    private lateinit var parser: YouTubeContentParser
    private lateinit var jsonParser: YouTubeJsonParser

    @Before
    fun setup() {
        jsonParser = YouTubeJsonParser()
        parser = YouTubeContentParser(jsonParser)
    }

    @Test
    fun testParseSearchResultsSeparatesEntitiesStrictly() {
        // Mock InnerTube Search response containing:
        // - Songs shelf with real video tracks
        // - Albums shelf with real MPRE albums
        // - Artists shelf with UC channels
        // - Playlists shelf with VLPL playlists
        // - Videos shelf (should be ignored)
        val mockResponse = """
        {
            "contents": {
                "tabbedSearchResultsRenderer": {
                    "tabs": [
                        {
                            "tabRenderer": {
                                "content": {
                                    "sectionListRenderer": {
                                        "contents": [
                                            {
                                                "musicShelfRenderer": {
                                                    "title": { "runs": [{ "text": "Songs" }] },
                                                    "contents": [
                                                        {
                                                            "musicResponsiveListItemRenderer": {
                                                                "flexColumns": [
                                                                    {
                                                                        "musicResponsiveListItemFlexColumnRenderer": {
                                                                            "text": {
                                                                                "runs": [
                                                                                    {
                                                                                        "text": "Green Flag",
                                                                                        "navigationEndpoint": {
                                                                                            "watchEndpoint": { "videoId": "vid_song_1" }
                                                                                        }
                                                                                    }
                                                                                ]
                                                                            }
                                                                        }
                                                                    },
                                                                    {
                                                                        "musicResponsiveListItemFlexColumnRenderer": {
                                                                            "text": {
                                                                                "runs": [
                                                                                    {
                                                                                        "text": "Vikram Sarkar",
                                                                                        "navigationEndpoint": {
                                                                                            "browseEndpoint": {
                                                                                                "browseId": "UC_artist_vikram",
                                                                                                "browseEndpointContextSupportedConfigs": {
                                                                                                    "browseEndpointContextMusicConfig": {
                                                                                                        "pageType": "MUSIC_PAGE_TYPE_ARTIST"
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    },
                                                                                    { "text": " • " },
                                                                                    { "text": "Green Flag EP" },
                                                                                    { "text": " • " },
                                                                                    { "text": "3:20" }
                                                                                ]
                                                                            }
                                                                        }
                                                                    }
                                                                ]
                                                            }
                                                        }
                                                    ]
                                                }
                                            },
                                            {
                                                "musicShelfRenderer": {
                                                    "title": { "runs": [{ "text": "Albums" }] },
                                                    "contents": [
                                                        {
                                                            "musicResponsiveListItemRenderer": {
                                                                "navigationEndpoint": {
                                                                    "browseEndpoint": {
                                                                        "browseId": "MPREb_album_green_flag",
                                                                        "browseEndpointContextSupportedConfigs": {
                                                                            "browseEndpointContextMusicConfig": {
                                                                                "pageType": "MUSIC_PAGE_TYPE_ALBUM"
                                                                            }
                                                                        }
                                                                    }
                                                                },
                                                                "flexColumns": [
                                                                    {
                                                                        "musicResponsiveListItemFlexColumnRenderer": {
                                                                            "text": { "runs": [{ "text": "Green Flag Album" }] }
                                                                        }
                                                                    },
                                                                    {
                                                                        "musicResponsiveListItemFlexColumnRenderer": {
                                                                            "text": {
                                                                                "runs": [
                                                                                    { "text": "Album" },
                                                                                    { "text": " • " },
                                                                                    {
                                                                                        "text": "Vikram Sarkar",
                                                                                        "navigationEndpoint": {
                                                                                            "browseEndpoint": {
                                                                                                "browseId": "UC_artist_vikram",
                                                                                                "browseEndpointContextSupportedConfigs": {
                                                                                                    "browseEndpointContextMusicConfig": {
                                                                                                        "pageType": "MUSIC_PAGE_TYPE_ARTIST"
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    },
                                                                                    { "text": " • " },
                                                                                    { "text": "2024" }
                                                                                ]
                                                                            }
                                                                        }
                                                                    }
                                                                ]
                                                            }
                                                        }
                                                    ]
                                                }
                                            },
                                            {
                                                "musicShelfRenderer": {
                                                    "title": { "runs": [{ "text": "Artists" }] },
                                                    "contents": [
                                                        {
                                                            "musicResponsiveListItemRenderer": {
                                                                "navigationEndpoint": {
                                                                    "browseEndpoint": {
                                                                        "browseId": "UC_artist_vikram",
                                                                        "browseEndpointContextSupportedConfigs": {
                                                                            "browseEndpointContextMusicConfig": {
                                                                                "pageType": "MUSIC_PAGE_TYPE_ARTIST"
                                                                            }
                                                                        }
                                                                    }
                                                                },
                                                                "flexColumns": [
                                                                    {
                                                                        "musicResponsiveListItemFlexColumnRenderer": {
                                                                            "text": { "runs": [{ "text": "Vikram Sarkar" }] }
                                                                        }
                                                                    },
                                                                    {
                                                                        "musicResponsiveListItemFlexColumnRenderer": {
                                                                            "text": { "runs": [{ "text": "1.2M subscribers" }] }
                                                                        }
                                                                    }
                                                                ]
                                                            }
                                                        }
                                                    ]
                                                }
                                            },
                                            {
                                                "musicShelfRenderer": {
                                                    "title": { "runs": [{ "text": "Playlists" }] },
                                                    "contents": [
                                                        {
                                                            "musicResponsiveListItemRenderer": {
                                                                "navigationEndpoint": {
                                                                    "browseEndpoint": {
                                                                        "browseId": "VLPL_playlist_beats",
                                                                        "browseEndpointContextSupportedConfigs": {
                                                                            "browseEndpointContextMusicConfig": {
                                                                                "pageType": "MUSIC_PAGE_TYPE_PLAYLIST"
                                                                            }
                                                                        }
                                                                    }
                                                                },
                                                                "flexColumns": [
                                                                    {
                                                                        "musicResponsiveListItemFlexColumnRenderer": {
                                                                            "text": { "runs": [{ "text": "Green Flag Beats" }] }
                                                                        }
                                                                    },
                                                                    {
                                                                        "musicResponsiveListItemFlexColumnRenderer": {
                                                                            "text": {
                                                                                "runs": [
                                                                                    { "text": "Playlist" },
                                                                                    { "text": " • " },
                                                                                    { "text": "Beats Curator" },
                                                                                    { "text": " • " },
                                                                                    { "text": "50 songs" }
                                                                                ]
                                                                            }
                                                                        }
                                                                    }
                                                                ]
                                                            }
                                                        }
                                                    ]
                                                }
                                            },
                                            {
                                                "musicShelfRenderer": {
                                                    "title": { "runs": [{ "text": "Videos" }] },
                                                    "contents": [
                                                        {
                                                            "musicResponsiveListItemRenderer": {
                                                                "flexColumns": [
                                                                    {
                                                                        "musicResponsiveListItemFlexColumnRenderer": {
                                                                            "text": { "runs": [{ "text": "Green Flag Official Video" }] }
                                                                        }
                                                                    }
                                                                ]
                                                            }
                                                        }
                                                    ]
                                                }
                                            }
                                        ]
                                    }
                                }
                            }
                        }
                    ]
                }
            }
        }
        """.trimIndent()

        val results = parser.parseSearchResults(mockResponse)

        // 1. Verify Songs
        assertEquals(1, results.songs.size)
        assertEquals("vid_song_1", results.songs[0].id)
        assertEquals("Green Flag", results.songs[0].title)
        assertEquals("Vikram Sarkar", results.songs[0].artist)

        // 2. Verify Albums
        assertEquals(1, results.albums.size)
        assertEquals("MPREb_album_green_flag", results.albums[0].id)
        assertEquals("Green Flag Album", results.albums[0].title)
        assertEquals("Vikram Sarkar", results.albums[0].artist)
        assertEquals("2024", results.albums[0].year)

        // 3. Verify Artists
        assertEquals(1, results.artists.size)
        assertEquals("UC_artist_vikram", results.artists[0].id)
        assertEquals("Vikram Sarkar", results.artists[0].name)
        assertEquals("1.2M subscribers", results.artists[0].subscribers)

        // 4. Verify Playlists
        assertEquals(1, results.playlists.size)
        assertEquals("PL_playlist_beats", results.playlists[0].id)
        assertEquals("Green Flag Beats", results.playlists[0].title)
        assertEquals("Beats Curator", results.playlists[0].author)

        // 5. Verify no cross-mixing: Playlists do not contain Albums and Albums do not contain Playlists
        assertTrue(results.albums.none { it.id.startsWith("PL_") || it.id.startsWith("VLPL_") })
        assertTrue(results.playlists.none { it.id.startsWith("MPREb_") })
    }

    @Test
    fun testParseSearchAlbumsRejectsPlaylists() {
        val playlistJson = """
        {
            "musicResponsiveListItemRenderer": {
                "navigationEndpoint": {
                    "browseEndpoint": {
                        "browseId": "VLPL12345",
                        "browseEndpointContextSupportedConfigs": {
                            "browseEndpointContextMusicConfig": {
                                "pageType": "MUSIC_PAGE_TYPE_PLAYLIST"
                            }
                        }
                    }
                },
                "flexColumns": [
                    {
                        "musicResponsiveListItemFlexColumnRenderer": {
                            "text": { "runs": [{ "text": "My Chill Playlist" }] }
                        }
                    },
                    {
                        "musicResponsiveListItemFlexColumnRenderer": {
                            "text": { "runs": [{ "text": "Playlist • Naruto • 25 songs" }] }
                        }
                    }
                ]
            }
        }
        """.trimIndent()

        val albums = parser.parseSearchAlbums(playlistJson)
        assertTrue("Playlists must NOT be parsed into Albums", albums.isEmpty())

        val playlists = parser.parseSearchPlaylists(playlistJson)
        assertEquals(1, playlists.size)
        assertEquals("PL12345", playlists[0].id)
        assertEquals("My Chill Playlist", playlists[0].title)
        assertEquals("Naruto", playlists[0].author)
    }

    @Test
    fun testParseSearchPlaylistsRejectsAlbums() {
        val albumJson = """
        {
            "musicResponsiveListItemRenderer": {
                "navigationEndpoint": {
                    "browseEndpoint": {
                        "browseId": "MPREb_abcde",
                        "browseEndpointContextSupportedConfigs": {
                            "browseEndpointContextMusicConfig": {
                                "pageType": "MUSIC_PAGE_TYPE_ALBUM"
                            }
                        }
                    }
                },
                "flexColumns": [
                    {
                        "musicResponsiveListItemFlexColumnRenderer": {
                            "text": { "runs": [{ "text": "Awesome Album" }] }
                        }
                    },
                    {
                        "musicResponsiveListItemFlexColumnRenderer": {
                            "text": { "runs": [{ "text": "Album • Arijit Singh • 2023" }] }
                        }
                    }
                ]
            }
        }
        """.trimIndent()

        val playlists = parser.parseSearchPlaylists(albumJson)
        assertTrue("Albums must NOT be parsed into Playlists", playlists.isEmpty())

        val albums = parser.parseSearchAlbums(albumJson)
        assertEquals(1, albums.size)
        assertEquals("MPREb_abcde", albums[0].id)
        assertEquals("Awesome Album", albums[0].title)
        assertEquals("Arijit Singh", albums[0].artist)
        assertEquals("2023", albums[0].year)
    }
}
