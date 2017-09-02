package main
import "github.com/tomasen/fcgi_client"
import "log"
import "io/ioutil"
import "os"
import "net/http"

type reqResp struct {
	r *http.Request
	respChan chan codeContent
}

type codeContent struct {
	content []byte
	code int	
}

func handleReq(fcgi *fcgiclient.FCGIClient, r *http.Request, respChan chan codeContent) {
	defer func() {
		if err := recover(); err != nil {
			log.Println("ERROR IN DOWNSTREAM COMM", err)
			respChan <- codeContent{[]byte("ERROR"),500}
			panic(err)
		}
	}()
	// log.Println("received request from", r.URL.Path)

	env := make(map[string]string)
	env["SCRIPT_FILENAME"] = "/home/www/test.php"
	env["SERVER_SOFTWARE"] = "go / fcgiclient "
	env["REMOTE_ADDR"] = "127.0.0.1"
	env["QUERY_STRING"] = ""
	env["PATH_INFO"] = r.URL.Path

	// log.Println("sending binary request downstream")
	resp, err := fcgi.Get(env)
	if err != nil {
			log.Println("err:", err)
	}
	
	// log.Println("awaiting binary response from downstream")
	content, err := ioutil.ReadAll(resp.Body)
	if err != nil {
			log.Println("err:", err)
	}
	// log.Println("got binary response from downstream", content)
	respChan <- codeContent{content,200}
	// log.Println("done")
}

func fcgiWorker(requests chan reqResp) {
	// log.Println("Connecting to ", os.Args[2])
	fcgi, err := fcgiclient.Dial("unix", os.Args[2])
	if err != nil {
			log.Println("err:", err)
	}

	defer func() { 
		err := recover()
		log.Println("Got an error in worker loop, trying to recover", err)
		fcgiWorker(requests)
	}()

	for {
		reqResp := <-requests
		handleReq(fcgi, reqResp.r, reqResp.respChan)
	}
}

func main() {
	requests := make (chan reqResp)
	for i := 0; i < 4; i++ {
		go fcgiWorker(requests)
	}
	handler := func (w http.ResponseWriter, r *http.Request) {
		// log.Println("sending request to worker")
		respChan := make(chan codeContent)
		requests <- reqResp{r, respChan}
		// log.Println("awaiting response from worker")
		resp := <-respChan
		// log.Println("writing response upstream")
		w.WriteHeader(resp.code)
		w.Write(resp.content)
		// log.Println("done.")
	}
	http.HandleFunc("/", handler)
	http.ListenAndServe(os.Args[1], nil)
}