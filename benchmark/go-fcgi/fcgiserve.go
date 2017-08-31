package main
import "fmt"
import "github.com/tomasen/fcgi_client"
import "log"
import "io/ioutil"
import "os"
import "net/http"

func debugHandler(w http.ResponseWriter, r *http.Request) {
	log.Println(r.URL.Path)
	fmt.Fprintf(w, "hello, you've hit %s\n", r.URL.Path)
}

func fcgiWorker(requests chan *http.Request, responses chan []byte) {
	log.Println("Connecting to ", os.Args[2])
	fcgi, err := fcgiclient.Dial("unix", os.Args[2])
	if err != nil {
			log.Println("err:", err)
	}

	defer func() { 
		log.Println("Got an error, trying to recover")
		responses <- []byte("ERROR")
		fcgiWorker(requests,responses)
	}()

	for {
		r := <-requests
		log.Println("received request from", r.URL.Path)

		env := make(map[string]string)
		env["SCRIPT_FILENAME"] = "/home/www/test.php"
		env["SERVER_SOFTWARE"] = "go / fcgiclient "
		env["REMOTE_ADDR"] = "127.0.0.1"
		env["QUERY_STRING"] = ""
		env["PATH_INFO"] = r.URL.Path

		log.Println("sending binary request downstream")
		resp, err := fcgi.Get(env)
		if err != nil {
				log.Println("err:", err)
		}
		
		log.Println("awaiting binary response from downstream")
		content, err := ioutil.ReadAll(resp.Body)
		if err != nil {
				log.Println("err:", err)
		}
		log.Println("got binary response from downstream")
		responses <- content
	}
}

func main() {


	requests := make (chan *http.Request)
	response := make (chan []byte)

	go fcgiWorker(requests, response)

	handler := func (w http.ResponseWriter, r *http.Request) {
		log.Println("sending request to worker")
		requests <- r
		log.Println("awaiting response from worker")
		resp := <-response
		log.Println("writing response upstream")
		w.Write(resp)
		log.Println("done.")
	}

	// handler := func (w http.ResponseWriter, r *http.Request) {
	// 	log.Println("received request from", r.URL.Path)
	// 	// fmt.Fprintf(w, "making request to %s\n", r.URL.Path)

	// 	env := make(map[string]string)
	// 	env["SCRIPT_FILENAME"] = "/home/www/test.php"
	// 	env["SERVER_SOFTWARE"] = "go / fcgiclient "
	// 	env["REMOTE_ADDR"] = "127.0.0.1"
	// 	env["QUERY_STRING"] = ""
	// 	env["PATH_INFO"] = r.URL.Path

	// 	resp, err := fcgi.Get(env)
	// 	if err != nil {
	// 			log.Println("err:", err)
	// 	}

	// 	content, err := ioutil.ReadAll(resp.Body)
	// 	if err != nil {
	// 			log.Println("err:", err)
	// 	}
	// 	log.Println("received response: ", content)
	// 	w.Write(content)
	// }

	http.HandleFunc("/", handler)
	http.ListenAndServe(os.Args[1], nil)
}