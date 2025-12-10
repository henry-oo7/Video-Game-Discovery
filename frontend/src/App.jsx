import {useState,useEffect,useRef} from "react";

export const API_BASE = "https://videogamerecs.link";

function App() {
    const [searchTerm, setSearchTerm] = useState("")
    const [games, setGames] = useState([])
    const [filters, setFilters] = useState({genre: null, platform: null})
    const [visitorId] = useState(() => {
        // 1. Try to read from storage
        let savedId = localStorage.getItem("visitorId");

        // 2. If missing, generate and save immediately
        if (!savedId) {
            savedId = crypto.randomUUID();
            localStorage.setItem("visitorId", savedId);
        }

        // 3. Return the value to React State
        return savedId;
    });
    const [favorites, setFavorites] = useState([])
    const [userName, setUserName] = useState("")
    const [hasVoted, sethasVoted] = useState(false);
    const [genresList, setGenresList] = useState([]);
    const [platformList, setPlatformList] = useState([]);
    const [page, setPage] = useState(0);
    const loaderRef = useRef(null);
    const [hasMore, setHasMore] = useState(true);
    const [isLoading, setIsLoading] = useState(false);
    const [showTopBtn, setShowTopBtn] = useState(false);
    const [recommendations, setRecommendations] = useState([]);
    const [isRecsOpen, setIsRecsOpen] = useState(false);
    const [isRecsLoading, setIsRecsLoading] = useState(false);
    const [showWelcome, setShowWelcome] = useState(true);

    const toggleFavorites = (game) => {
        if (favorites.some(fav => fav.id === game.id)) {
            setFavorites(favorites.filter(fav => fav.id !== game.id));
        } else {
            if (favorites.length < 5) {
                setFavorites([...favorites, game]);
            } else {
                alert("you can only choose 5 favorites")
            }
        }
    };

    const submitFavorites = () => {
        console.log("🖱️ SUBMIT CLICKED. visitorId is:", visitorId);
        if (!userName) {
            alert("Please enter a username");
            return;
        }

        const payload = {
            visitorId: visitorId, // ✅ Fixed spelling
            name: userName,
            gameIds: favorites.map(fav => fav.id), // ✅ Fixed camelCase
        };

        fetch(`${API_BASE}/users/favorites`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(payload),
        })
            .then(response => {
                if (response.ok) {
                    alert("Favorites saved successfully.");
                    setFavorites([]);
                    setUserName("");
                    sethasVoted(true);
                    fetchRecommendations(visitorId)
                } else {
                    alert("Failed to save favorites.");
                }
            })
            .catch(error => console.log(error));
    };

    const fetchRecommendations = (id) => {
        setIsRecsLoading(true);
        fetch(`${API_BASE}/users/recommendations?visitorId=${id}`)
            .then(res => res.json())
            .then(data => {
                setRecommendations(data);
                setIsRecsLoading(false);
            })
            .catch(e => {
                console.error(e);
                setIsRecsLoading(false);
            });
    }

    const scrollToTop = () => {
        window.scrollTo({ top: 0, behavior: "smooth" });
    };

    useEffect(() => {
        if (visitorId) {
            fetch(`${API_BASE}/users/has-voted?visitorId=${visitorId}`)
                .then(response => response.json())
                .then(alreadyVoted => {
                    if (alreadyVoted) {
                        sethasVoted(true);
                        setShowWelcome(false); // <--- 🚀 AUTO-SKIP for returning users!

                        // ... (keep your existing fetch favorites/recs logic here) ...
                        fetch(`${API_BASE}/users/favorites?visitorId=${visitorId}`)
                            .then(res => res.json())
                            .then(data => setFavorites(data))
                            .catch(err => console.error(err));

                        fetchRecommendations(visitorId);
                    }
                })
                .catch(error => console.error("Error:", error));
        }
    }, [visitorId]);

    {/*back to the top button*/}
    useEffect(() => {
        const handleScroll = () => {
            if (window.scrollY > 300) {
                setShowTopBtn(true);
            } else {
                setShowTopBtn(false);
            }
        };

        window.addEventListener("scroll", handleScroll);
        return () => window.removeEventListener("scroll", handleScroll);
    }, []);


    {/*fetching the genres and platforms for the filters*/
    }
    useEffect(() => {
        const fetchFilters = async () => {
            try {
                const genreRes = await fetch(`${API_BASE}/videogames/genres`);
                const platformRes = await fetch(`${API_BASE}/videogames/platforms`);

                const genres = await genreRes.json();
                const platforms = await platformRes.json();

                setGenresList(genres);
                setPlatformList(platforms);
            } catch (error) {
                console.log("Failed to load filters", error);
            }
        };
        fetchFilters();
    }, [])

    {/*Automatically runs when filter or searchTerm changes to fetch the most accurate data*/
    }
    useEffect(() => {

        const controller = new AbortController();
        const signal = controller.signal;
        const url = new URL(`${API_BASE}/videogames`);

        url.searchParams.append("page", page);
        url.searchParams.append("size", 20);

        // 2. Append params ONLY if they exist
        if (searchTerm) url.searchParams.append("startsWith", searchTerm);
        if (filters.genre) url.searchParams.append("genre", filters.genre);
        if (filters.platform) url.searchParams.append("platform", filters.platform);

        setIsLoading(true);

        // 3. Fetch the data
        fetch(url, {signal: signal})
            .then(response => response.json())
            .then(data => {
                if (page === 0) {
                    setGames(data.content);
                } else {
                    setGames(prevGames => [...prevGames, ...data.content]);
                }
                setHasMore(!data.last);
                setIsLoading(false);
            })
            .catch(error => {
                if (error.name !== "AbortError") {
                    console.error("Error", error);
                }
            });

        return () => {
            controller.abort();
        };
    }, [searchTerm, filters, page]);

    {/*Automatically run to see if user has already voted and displays thank you message*/
    }
    useEffect(() => {
        if (visitorId) {
            fetch(`${API_BASE}/users/has-voted?visitorId=${visitorId}`)
                .then(response => response.json())
                .then(alreadyVoted => {
                    if (alreadyVoted) {
                        sethasVoted(true);

                        // 1. Restore their Top 5 Picks
                        fetch(`${API_BASE}/users/favorites?visitorId=${visitorId}`)
                            .then(res => res.json())
                            .then(data => setFavorites(data))
                            .catch(err => console.error(err));

                        // 2. Restore their Recommendations! 🌟
                        // (We reuse the function we made)
                        fetchRecommendations(visitorId);
                    }
                })
                .catch(error => console.error("Error:", error));
        }
    }, [visitorId]);

    {/*Watches to see if user at bottom of page to load the next list of games*/
    }
    useEffect(() => {
        if (!hasMore || isLoading) return;

        const observer = new IntersectionObserver(entries => {
            const first = entries[0];
            if (first.isIntersecting && !isLoading) {
                setPage((prev) => prev + 1);
            }
        }, {
            threshold: 0.1,
            rootMargin: "500px" // <--- ADD THIS! ⚡️
            // This says: "Trigger when the watcher is within 500px of the screen"
        });

        if (loaderRef.current) {
            observer.observe(loaderRef.current);
        }

        return () => {
            if (loaderRef.current) {
                observer.unobserve(loaderRef.current);
            }
        };
    }, [hasMore, isLoading, games.length]);

    //  1. THE WELCOME SCREEN
    if (showWelcome) {
        return (
            <div className="min-h-screen bg-slate-900 text-slate-100 font-sans flex items-center justify-center p-4 relative overflow-hidden">

                {/* Background Decor (Blue/Cyan Glows) */}
                <div className="absolute top-[-10%] left-[-10%] w-96 h-96 bg-blue-600/20 rounded-full blur-[100px] pointer-events-none animate-pulse" />
                <div className="absolute bottom-[-10%] right-[-10%] w-96 h-96 bg-cyan-600/20 rounded-full blur-[100px] pointer-events-none animate-pulse" />

                {/* The Card */}
                <div className="max-w-md w-full bg-slate-800/80 backdrop-blur-xl p-8 rounded-3xl shadow-2xl border border-cyan-500/30 text-center relative z-10 animate-fade-in-up">

                    <h1 className="text-5xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-blue-600 mb-2 tracking-tight drop-shadow-lg">
                        Video Game Discovery
                    </h1>

                    <p className="text-cyan-200/60 text-xs font-bold uppercase tracking-[0.2em] mb-8">
                        Your Personal Curator
                    </p>

                    <div className="space-y-6">
                        <div className="bg-slate-900/50 p-5 rounded-2xl border border-cyan-500/20 text-left shadow-inner">
                            <h3 className="text-white font-bold mb-3 flex items-center gap-2 text-lg">
                                <span className="text-2xl">🎮</span> How it works
                            </h3>
                            <ul className="text-sm text-slate-300 space-y-2.5">
                                <li className="flex gap-3 items-start">
                                    <span className="text-cyan-400 font-bold bg-cyan-500/10 px-1.5 rounded text-xs py-0.5">1</span>
                                    <span>Search & Filter to find games you love.</span>
                                </li>
                                <li className="flex gap-3 items-start">
                                    <span className="text-cyan-400 font-bold bg-cyan-500/10 px-1.5 rounded text-xs py-0.5">2</span>
                                    <span>Select your <span className="text-white font-bold border-b border-cyan-500">Top 5</span> favorites.</span>
                                </li>
                                <li className="flex gap-3 items-start">
                                    <span className="text-cyan-400 font-bold bg-cyan-500/10 px-1.5 rounded text-xs py-0.5">3</span>
                                    <span>Get AI-powered recommendations.</span>
                                </li>
                            </ul>
                        </div>

                        <div className="flex flex-col gap-3">
                            <label className="text-left text-[10px] font-bold text-cyan-400 uppercase tracking-wider ml-1">
                                Enter your name to start
                            </label>
                            <input
                                type="text"
                                placeholder="e.g. Player 1"
                                className="w-full bg-slate-900 border border-cyan-500/50 p-3 rounded-xl text-white focus:ring-2 focus:ring-cyan-500 outline-none text-center text-lg placeholder-slate-700 transition-all shadow-inner"
                                value={userName}
                                onChange={(e) => setUserName(e.target.value)}
                                onKeyDown={(e) => e.key === 'Enter' && userName && setShowWelcome(false)}
                            />
                            <button
                                onClick={() => {
                                    if (userName.trim()) setShowWelcome(false);
                                    else alert("Please enter your name!");
                                }}
                                className={`w-full py-3.5 rounded-xl font-bold uppercase tracking-widest transition-all duration-300 transform ${
                                    userName
                                        ? "bg-gradient-to-r from-cyan-600 to-blue-600 text-white shadow-lg shadow-cyan-500/40 hover:scale-[1.02] hover:shadow-cyan-500/60"
                                        : "bg-slate-700 text-slate-500 cursor-not-allowed border border-slate-600"
                                }`}
                                disabled={!userName}
                            >
                                Let's Go 🚀
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-slate-900 text-slate-100 p-8 font-sans">

            {/* 1. Header & Search */}
            <div className="max-w-6xl mx-auto mb-8">
                <h1 className="text-5xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-blue-500 mb-6 text-center tracking-tight">
                    Video Game Discovery
                </h1>

                <div className="bg-slate-800 p-4 rounded-xl shadow-2xl flex flex-col md:flex-row gap-4 items-center w-full border border-slate-700">
                    <span className="font-bold text-cyan-400 whitespace-nowrap">Search Game:</span>

                    <input
                        type="text"
                        className="bg-slate-900 border border-slate-700 text-white p-2 rounded-lg flex-1 w-full focus:outline-none focus:ring-2 focus:ring-cyan-500 transition"
                        placeholder="Type a name..."
                        value={searchTerm}
                        onChange={(e) => {setSearchTerm(e.target.value); setPage(0);}}
                    />

                    <div className="flex gap-2 w-full md:w-auto">
                        <select
                            className="bg-slate-900 border border-slate-700 text-white text-sm p-2 rounded-lg w-1/2 md:w-40 cursor-pointer focus:outline-none focus:ring-2 focus:ring-cyan-500 truncate"
                            value={filters.genre || ""}
                            onChange={(e) => {setFilters({...filters, genre: e.target.value || null}); setPage(0);}}
                        >
                            <option value="">All Genres</option>
                            {genresList.map((genre) => (
                                <option key={genre.id} value={genre.name}>{genre.name}</option>
                            ))}
                        </select>

                        <select
                            className="bg-slate-900 border border-slate-700 text-white text-sm p-2 rounded-lg w-1/2 md:w-40 cursor-pointer focus:outline-none focus:ring-2 focus:ring-cyan-500 truncate"
                            value={filters.platform || ""}
                            onChange={(e) => {setFilters({ ...filters, platform: e.target.value || null }); setPage(0);}}
                        >
                            <option value="">All Platforms</option>
                            {platformList.map(p => (
                                <option key={p.id} value={p.name}>{p.name}</option>
                            ))}
                        </select>
                    </div>

                    {/* 3. Reset Button */}
                    <button
                        onClick={() => {
                            setSearchTerm("");
                            setFilters({ genre: null, platform: null });
                            setPage(0);
                        }}
                        className="p-2 rounded-lg bg-slate-700 text-slate-300 hover:bg-red-500 hover:text-white transition-all duration-200 ease-out font-bold px-4 shadow-md border border-slate-600 hover:border-red-400 hover:px-6 hover:shadow-[0_0_15px_rgba(239,68,68,0.4)] whitespace-nowrap"
                        title="Reset Search"
                    >
                        ✕ Reset
                    </button>
                </div>
            </div>

            {/* 2. Top 5 Showcase Bar */}
            <div className="fixed bottom-6 inset-x-0 mx-auto z-50 w-[96%] md:w-max max-w-[90rem] bg-slate-800/95 backdrop-blur-md p-2 md:p-4 rounded-3xl shadow-2xl border border-slate-600/50 transition-all duration-300 hover:shadow-cyan-500/20 hover:scale-[1.01]">
                {/* Justify-between helps spread items on tiny screens, centered on desktop */}
                <div className="flex items-center justify-between md:justify-center gap-2 md:gap-6">

                    {/* Desktop Only: Trophy Icon (Unchanged) */}
                    <div className="hidden md:flex flex-col justify-center items-center px-5 border-r border-slate-600/50 h-full">
                        <span className="text-3xl leading-none">🏆</span>
                        <span className="text-sm font-bold text-slate-400 uppercase tracking-widest mt-1">Picks</span>
                    </div>

                    {/* Game Slots Container */}
                    {/* Changed: gap-1.5 for mobile, gap-4 for desktop */}
                    <div className="flex gap-1.5 md:gap-4 flex-1 md:flex-none justify-center">
                        {[...Array(5)].map((_, index) => {
                            const game = favorites[index];
                            return (
                                <div
                                    key={index}
                                    // Changed: w-11 (44px) on mobile, w-28 (112px) on desktop
                                    className="flex flex-col items-center gap-1.5 w-11 sm:w-14 md:w-28 group"
                                    onClick={() => !hasVoted && game && toggleFavorites(game)}
                                >
                                    {/* Aspect Ratio Box */}
                                    <div className={`relative w-full aspect-[3/4] rounded-lg md:rounded-xl overflow-hidden flex items-center justify-center transition-all duration-200 border-2 ${
                                        game
                                            ? `border-cyan-500 shadow-lg bg-slate-900 ${!hasVoted ? "cursor-pointer" : ""}`
                                            : "border-slate-600 bg-slate-900/40 border-dashed"
                                    }`}>
                                        {game ? (
                                            <>
                                                <img src={game.coverUrl} alt={game.name} className="w-full h-full object-cover" />

                                                {!hasVoted && (
                                                    <div className="absolute inset-0 bg-black/80 flex items-center justify-center opacity-0 group-hover:opacity-100 transition backdrop-blur-[1px]">
                                                        {/* Smaller 'X' on mobile */}
                                                        <span className="text-red-500 font-bold text-xl md:text-4xl">×</span>
                                                    </div>
                                                )}
                                            </>
                                        ) : (
                                            /* Smaller Number on mobile */
                                            <span className="text-slate-600 font-bold text-lg md:text-2xl">{index + 1}</span>
                                        )}
                                    </div>

                                    {/* Game Title Under Slot */}
                                    <div className="h-6 md:h-8 w-full flex items-start justify-center">
                                        {/* Smaller font (8px) on mobile, original (xs) on desktop */}
                                        <span className={`text-[8px] md:text-xs font-medium text-center leading-tight line-clamp-2 ${game ? "text-slate-200 group-hover:text-cyan-400 transition-colors" : "invisible"}`}>
                                            {game ? game.name : "Placeholder"}
                                        </span>
                                    </div>
                                </div>
                            );
                        })}
                    </div>

                    {/* Action Button Area */}
                    <div className={`flex flex-col justify-center overflow-hidden transition-all duration-500 ease-in-out ${
                        favorites.length === 5 || hasVoted
                            // Changed: Smaller max-width and padding on mobile
                            ? "max-w-[100px] md:max-w-[200px] opacity-100 ml-1 md:ml-2 border-l border-slate-600/50 pl-2 md:px-6"
                            : "max-w-0 opacity-0"
                    }`}>
                        {hasVoted ? (
                            <div className="flex flex-col items-center gap-1 w-full">
                                <span className="text-green-400 font-bold text-[8px] md:text-xs uppercase tracking-widest text-center whitespace-nowrap">
                                    {/* Shortened text for mobile */}
                                    <span className="md:hidden">Done!</span>
                                    <span className="hidden md:inline">Thanks for voting!</span>
                                </span>

                                {isRecsLoading ? (
                                    <div className="text-cyan-400 text-[10px] md:text-xs animate-pulse font-bold uppercase py-2 text-center whitespace-nowrap">
                                        Loading...
                                    </div>
                                ) : (
                                    <button
                                        onClick={() => setIsRecsOpen(true)}
                                        className="bg-gradient-to-r from-purple-600 to-blue-600 text-white px-2 py-1.5 md:px-4 md:py-2 rounded-lg md:rounded-xl shadow-lg hover:scale-105 transition-transform flex items-center gap-1 md:gap-2 w-full justify-center whitespace-nowrap"
                                    >
                                        <span className="text-sm md:text-lg">✨</span>
                                        <div className="flex flex-col items-start leading-none">
                                            <span className="hidden md:inline text-[9px] uppercase font-bold text-blue-200">For You</span>
                                            <span className="text-[10px] md:text-xs font-bold">Recs</span>
                                        </div>
                                    </button>
                                )}
                            </div>
                        ) : (
                            <div className="w-full md:w-32 mx-auto flex items-center">
                                <button
                                    className="w-full py-2 md:py-2.5 rounded-lg md:rounded-xl text-[10px] md:text-xs font-bold uppercase tracking-wider bg-gradient-to-r from-cyan-600 to-blue-600 text-white shadow-lg shadow-cyan-500/20 hover:shadow-cyan-500/40 hover:scale-105 transition-all duration-200 px-1"
                                    onClick={submitFavorites}
                                >
                                    {/* "Confirm" on mobile, "Confirm Picks" on desktop */}
                                    <span className="md:hidden">Confirm</span>
                                    <span className="hidden md:inline">Confirm Picks</span>
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* 3. Main Game Grid */}
            <div className="max-w-6xl mx-auto">
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6">
                    {games.map((game, index) => ( // <--- Add 'index' here!
                        <div
                            key={game.id}
                            onClick={() => toggleFavorites(game)}

                            className={`group cursor-pointer flex flex-col gap-3 animate-fade-in-up`}

                            style={{ animationDelay: `${index * 50}ms` }}
                        >
                            <div className={`relative w-full aspect-[3/4] rounded-xl overflow-hidden shadow-2xl transition-all duration-300 ease-out transform group-hover:scale-[1.02] group-hover:shadow-cyan-500/50 ${
                                favorites.some(fav => fav.id === game.id)
                                    ? "ring-4 ring-cyan-500 shadow-[0_0_20px_rgba(6,182,212,0.6)]"
                                    : "bg-slate-800"
                            }`}>

                                {game.coverUrl ? (
                                    <img src={game.coverUrl} alt={game.name} className="w-full h-full object-cover transition-transform duration-500 ease-out group-hover:scale-[1.05]" />
                                ) : (
                                    <div className="w-full h-full flex flex-col items-center justify-center text-slate-600 font-bold bg-slate-800">
                                        <span>No Art</span>
                                    </div>
                                )}

                                <div className="absolute inset-0 bg-slate-900/90 backdrop-blur-[2px] flex flex-col items-center justify-center p-4 text-center opacity-0 group-hover:opacity-100 transition-all duration-300 ease-out">
                                    <div className="transform translate-y-4 group-hover:translate-y-0 transition-transform duration-300 ease-out">

                                        <div className="mb-2">
                                            <span className="text-cyan-400 text-[10px] font-bold uppercase tracking-widest block mb-0.5">Genres</span>
                                            <p className="text-white text-xs font-medium leading-relaxed line-clamp-1">
                                                {game.genres && game.genres.length > 0 ? game.genres.map(g => g.name).join(", ") : "N/A"}
                                            </p>
                                        </div>

                                        <div className="mb-2">
                                            <span className="text-cyan-400 text-[10px] font-bold uppercase tracking-widest block mb-0.5">Released</span>
                                            <p className="text-white text-xs font-medium">
                                                {game.firstReleaseDate
                                                    ? new Date(game.firstReleaseDate * 1000).getFullYear()
                                                    : "TBA"}
                                            </p>
                                        </div>

                                        <div>
                                            <span className="text-cyan-400 text-[10px] font-bold uppercase tracking-widest block mb-0.5">Platforms</span>
                                            <p className="text-slate-300 text-[10px] leading-relaxed line-clamp-2">
                                                {game.platforms && game.platforms.length > 0 ? game.platforms.map(p => p.name).join(", ") : "N/A"}
                                            </p>
                                        </div>

                                    </div>
                                </div>

                                {favorites.some(fav => fav.id === game.id) && (
                                    <div className="absolute top-2 right-2 bg-cyan-500 text-white rounded-full w-6 h-6 flex items-center justify-center shadow-lg font-bold text-xs z-10">
                                        ✓
                                    </div>
                                )}
                            </div>

                            <h3 className="text-sm font-bold text-slate-300 group-hover:text-cyan-400 transition-colors line-clamp-3 text-center leading-tight">
                                {game.name}
                            </h3>
                        </div>
                    ))}
                </div>

                {hasMore && games.length > 0 && (
                    <div ref={loaderRef} className="h-24 mt-12 flex items-center justify-center space-x-2">
                        <div className="w-3 h-3 bg-cyan-500 rounded-full animate-bounce"></div>
                        <div className="w-3 h-3 bg-cyan-500 rounded-full animate-bounce [animation-delay:0.1s]"></div>
                        <div className="w-3 h-3 bg-cyan-500 rounded-full animate-bounce [animation-delay:0.2s]"></div>
                    </div>
                )}
            </div>

            {/* ⬆️ Back to Top Button */}
            <button
                onClick={scrollToTop}
                className={`fixed bottom-8 right-8 z-40 p-3 rounded-full bg-cyan-500 text-white shadow-lg transition-all duration-300 hover:bg-cyan-400 hover:scale-110 hover:shadow-cyan-500/50 ${
                    showTopBtn ? "opacity-100 translate-y-0" : "opacity-0 translate-y-10 pointer-events-none"
                }`}
                aria-label="Back to Top"
            >
                {/* Simple Arrow Icon (SVG) */}
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M5 10l7-7m0 0l7 7m-7-7v18" />
                </svg>
            </button>

            {/* Recommendation Drawer */}
            <div
                className={`fixed inset-0 bg-black/60 z-[60] transition-opacity duration-300 ${
                    isRecsOpen ? "opacity-100 pointer-events-auto" : "opacity-0 pointer-events-none"
                }`}
                onClick={() => setIsRecsOpen(false)} // Click outside to close
            />

            <div
                className={`fixed top-0 right-0 h-full w-full md:w-96 bg-slate-900 border-l border-slate-700 shadow-2xl z-[70] transform transition-transform duration-300 ease-out p-6 overflow-y-auto ${
                    isRecsOpen ? "translate-x-0" : "translate-x-full"
                }`}
            >
                <div className="flex items-center justify-between mb-8">
                    <div>
                        <h2 className="text-2xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-purple-400 to-blue-400">
                            Recommended
                        </h2>
                        <p className="text-slate-400 text-xs mt-1">Based on your favorites</p>
                    </div>
                    <button
                        onClick={() => setIsRecsOpen(false)}
                        className="p-2 bg-slate-800 rounded-full hover:bg-slate-700 transition"
                    >
                        ✕
                    </button>
                </div>

                <div className="flex flex-col gap-4">
                    {recommendations.map((game, index) => (
                        <div
                            key={game.id}
                            className="flex gap-4 bg-slate-800/50 p-3 rounded-xl border border-slate-700 hover:border-purple-500/50 transition-colors cursor-pointer group"
                        >
                            <div className="w-16 h-20 flex-shrink-0 rounded-lg overflow-hidden bg-slate-900 shadow-md">
                                {game.coverUrl ? (
                                    <img src={game.coverUrl} className="w-full h-full object-cover" />
                                ) : (
                                    <div className="w-full h-full flex items-center justify-center text-[10px] text-slate-600">No Art</div>
                                )}
                            </div>

                            <div className="flex flex-col justify-center">
                                <h3 className="font-bold text-sm text-slate-200 leading-tight group-hover:text-purple-400 transition-colors">
                                    {game.name}
                                </h3>
                                <div className="flex flex-wrap gap-1 mt-2">
                                    {game.genres && game.genres.slice(0, 2).map(g => (
                                        <span key={g.id} className="text-[10px] bg-slate-700 px-1.5 py-0.5 rounded text-slate-300">
                                            {g.name}
                                        </span>
                                    ))}
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    )
}

export default App