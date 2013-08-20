package com.mattneary.imget

import org.scalatra._
import scalate.ScalateSupport
import scalaj.http.{Http, HttpOptions}

import scala.io.Source
import java.io._
import org.slf4j.{Logger, LoggerFactory}
import java.nio._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import java.io.{File,FileInputStream,FileOutputStream}

class ImgetServlet extends ImgetStack {
  val dest = "/Users/mattneary/Desktop/Open Source Products/imgurious/cache/"
  val logger = LoggerFactory.getLogger(getClass)

  def fetch(url: String, block: InputStream => Int) {
    Http(url).header("Authorization", "Client-ID b7c16c31dd48791").option(HttpOptions.connTimeout(10000)){in => block(in)}
  }
  def download(url: String, file: String) {
    fetch(url, { instream =>
      val out = new FileOutputStream((new File(file)))
      val buffer = new Array[Byte]( 1024 )
      Iterator 
      .continually(instream.read)
      .takeWhile(-1 !=)
      .foreach(out.write)
      out.close()
      1
    })
  }
  def renderPage(file: String) {
    org.scalatra.util.io.copy(new FileInputStream(file), response.getOutputStream)
  }
  def renderJSON(file: String) {
    val json = parse(io.Source.fromFile(file).mkString)    
    val rendered = (json \\ "link").children.map({ id =>
      for {
        JField("link", JString(link)) <- id
      } yield link
    }).foldLeft("") { (a,b) => 
      a + "<br>" + """<img src="/images/""" + b(0).split("""com\/""")(1) + """" />"""
    }
    response.getOutputStream.print(rendered)
    response.getOutputStream.close()
  }

  get("/") {
    contentType="text/html"
    if( !(new File(dest + "hot-viral.json")).exists ) {
      download("https://api.imgur.com/3/gallery/hot/viral/0.json", dest + "hot-viral.json")
      "Loading..."
    } else {
      val in = new FileInputStream((new File(dest + "../styles.css")))
      val out = response.getOutputStream()
      out.write("<style>".getBytes)
      Iterator 
      .continually(in.read)
      .takeWhile(-1 !=)
      .foreach(out.write)
      out.write("</style>".getBytes)
      renderJSON(dest + "hot-viral.json")
    } 
  }
  get("/images/*") {
    val rest = multiParams("splat")
    val image = rest.mkString("/")
    val file = new sun.misc.BASE64Encoder().encode(image.getBytes)
    if( !(new File(dest + file)).exists ) {
      contentType = "text/plaintext"
      response.getOutputStream.print("Loading...")
      response.getOutputStream.close()
      download("https://i.imgur.com/" + image, dest + file)
    } else {
      contentType = "text/html"
      renderPage(dest + file)
    }    
  }
  
}
