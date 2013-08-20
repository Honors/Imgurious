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
    // TODO: external template
    val json = parse(io.Source.fromFile(file).mkString)    
    val rendered = (json \\ "link").children.map({ id =>
      for {
        JField("link", JString(link)) <- id
      } yield link
      // TODO: early caching
    }).foldLeft("") { (a,b) => 
      a + """<div class="item"><img src="/images/""" + b(0).split("""com\/""")(1) + """" /></div>"""
    }
    response.getOutputStream.print(rendered)
    response.getOutputStream.close()
  }

  get("/") {
    redirect("/g/hot/viral")
  }
  get("/g/*") {
    val rest = multiParams("splat")
    contentType="text/html"
    val filter = rest.mkString("/")
    val name = filter.replace("/", "-")    
    
    if( !(new File(dest + name + ".json")).exists ) {
      download("https://api.imgur.com/3/gallery/" + filter + "/0.json", dest + name + ".json")
      "Loading..."
    } else {
      val in = new FileInputStream((new File(dest + "../styles.css")))
      val out = response.getOutputStream()
      out.write("""<link rel="stylesheet" href="/asset/styles.css">""".getBytes)
      out.write("""<img src="/asset/logo.png" class="hero">""".getBytes)
      renderJSON(dest + name + ".json")
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
  get("/asset/*") {
    val rest = multiParams("splat")
    val file = rest.mkString("/")
    renderPage(dest + "../" + file)
  }
}
