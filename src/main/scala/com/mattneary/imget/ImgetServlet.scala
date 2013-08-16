package com.mattneary.imget

import org.scalatra._
import scalate.ScalateSupport
import scalaj.http.{Http, HttpOptions}

import scala.io.Source
import java.io._
import org.slf4j.{Logger, LoggerFactory}
import java.nio._

class ImgetServlet extends ImgetStack {
  val dest = "/Users/mattneary/Desktop/imget/cache/"
  val logger = LoggerFactory.getLogger(getClass)

  def fetch(url: String, block: InputStream => Int) {
    Http("http://mattneary.com").option(HttpOptions.connTimeout(10000)){in => block(in)}
  }
  def download(url: String, file: String) {
    fetch(url, { instream =>
      val in = Source.fromInputStream(instream)
      val out = new java.io.PrintWriter(file)
      in.getLines.foreach({ part =>
        out.print(part)
      })
      out.close()
      1
    })
  }
  def render(file: String) {
    org.scalatra.util.io.copy(new FileInputStream(file), response.getOutputStream)
  }

  get("/") {
    contentType="text/html"
    Http("http://mattneary.com").option(HttpOptions.connTimeout(10000)).asString
  }
  get("/get/:image") {
    if( !(new File(dest ++ params("image"))).exists ) {
      contentType = "text/plaintext"
      download("http://mattneary.com", dest ++ params("image"))
      "Loading..."
    } else {
      contentType = "text/html"
      render(dest ++ params("image"))
    }    
  }
  
}
