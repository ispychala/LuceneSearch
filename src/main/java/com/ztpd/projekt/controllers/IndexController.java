package com.ztpd.projekt.controllers;

import com.ztpd.projekt.Result;
import com.ztpd.projekt.SearchQuery;
import com.ztpd.projekt.Searcher;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class IndexController {

    @RequestMapping("/")
    String index(Model model) {
        model.addAttribute("results", new ArrayList<Result>());
        model.addAttribute("searchQuery", new SearchQuery());
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
        return "index";
    }
}
