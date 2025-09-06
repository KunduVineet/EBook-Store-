import './App.css'
import AuthApp from "./Login.jsx";
import BookStore from "./components/BookStore.jsx";

function App() {
    return (
        <div className="min-h-screen w-full bg-white flex items-center justify-center bg-gradient-to-br from-blue-50 to-blue-100">
            <BookStore/>

        </div>
    )
}

export default App
