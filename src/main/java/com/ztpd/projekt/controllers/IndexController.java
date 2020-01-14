package com.ztpd.projekt.controllers;

import com.ztpd.projekt.Constants;
import com.ztpd.projekt.Result;
import com.ztpd.projekt.SearchQuery;
import com.ztpd.projekt.Searcher;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sun.misc.Request;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class IndexController {

    @RequestMapping("/")
    String index(Model model) {
        model.addAttribute("results", new ArrayList<Result>());
        model.addAttribute("searchQuery", new SearchQuery());
        model.addAttribute("file", new Result());
        return "index";
    }

    @RequestMapping("/search")
    String filter(@ModelAttribute("searchQuery") SearchQuery sq, Model model) {
        ArrayList<Result> resultList;
        try {
            resultList = Searcher.search(sq.getQuery());
        } catch (Exception e) {
            resultList = new ArrayList<Result>();
        }

        model.addAttribute("searchQuery", sq);
        model.addAttribute("results", resultList);
        model.addAttribute("file", new Result());
        return "index";
    }

    @RequestMapping(value = "/file/{filename}", method = RequestMethod.GET)
    @ResponseBody
    public FileSystemResource showFile(@PathVariable("filename") String filename) {
        String url = Constants.database + filename;
        return new FileSystemResource(new File(url));
    }
}
