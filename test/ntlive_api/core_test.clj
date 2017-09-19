(ns ntlive-api.core-test
  (:require [clojure.test :refer :all]
            [ntlive-api.core :refer :all]))

(deftest cities-parse
  (testing "cities are parsed correctly"
    (is (= (map
            (-> parsers :cities :parser)
            '({:tag :option, :attrs {:value "797"}, :content ("Aalborg\n")}
              {:tag :option, :attrs {:value "798"}, :content ("Brønderslev\n")}
              {:tag :option, :attrs {:value "799"}, :content ("Frederikshavn\n")}
              {:tag :option, :attrs {:value "800"}, :content ("Hjørring\n")}))
           
           '({:name "Aalborg", :id "797"}
             {:name "Brønderslev", :id "798"}
             {:name "Frederikshavn", :id "799"}
             {:name "Hjørring", :id "800"})))))

(deftest routes-parse
  (testing "routes are parsed correclty"
    (is (= (map
            (-> parsers :routes :parser)
            '({:tag :li, :attrs nil, :content ({:tag :a, :attrs {:href "http://ntlive.dk/rt/stop/9599"}, :content ("1: Grindsted/Langholt - Svenstrup/Godthåb")})}
              {:tag :li, :attrs nil, :content ({:tag :a, :attrs {:href "http://ntlive.dk/rt/stop/9600"}, :content ("2: Væddeløbsbanen - Karup/Storvorde/Gistrup")})}
              {:tag :li, :attrs nil, :content ({:tag :a, :attrs {:href "http://ntlive.dk/rt/stop/9803"}, :content ("18: Mod Aalborg Busterminal")})}))

           '({:name "Grindsted/Langholt - Svenstrup/Godthåb" :id "9599" :line "1"}
             {:name "Væddeløbsbanen - Karup/Storvorde/Gistrup" :id "9600" :line "2"}
             {:name "Mod Aalborg Busterminal", :id "9803", :line "18"})))))

(deftest stops-parse
  (testing "stops are parsed correctly"
    (is (= (map
            (-> parsers :stops :parser)
            '({:tag :li, :attrs nil, :content ({:tag :a, :attrs {:href "http://ntlive.dk/rt/destination/85100732"}, :content ("Væddeløbsbanen")})}
              {:tag :li, :attrs nil, :content ({:tag :a, :attrs {:href "http://ntlive.dk/rt/destination/85194852"}, :content ("Friluftsbadet")})}
              {:tag :li, :attrs nil, :content ({:tag :a, :attrs {:href "http://ntlive.dk/rt/destination/85110222"}, :content ("Steen Billes Gade")})}))
           
           '({:name "Væddeløbsbanen", :id "85100732"}
             {:name "Friluftsbadet", :id "85194852"}
             {:name "Steen Billes Gade", :id "85110222"})))))

(deftest times-parse
  (testing "times for a stop are correctly parsed"
    (is (= (map
            (-> parsers :times :parser)
            '({:tag :li, :attrs nil, :content ("22 mod Gistrup Skole" {:tag :br, :attrs nil, :content nil} "\n\t... 00.29\n\t\t")}
              {:tag :li, :attrs nil, :content ("22 mod Mølholm" {:tag :br, :attrs nil, :content nil} "\n\t... 00.53\n\t+2\t")}
              {:tag :li, :attrs nil, :content ("2 mod Væddeløbsbanen" {:tag :br, :attrs nil, :content nil} "\n\t... 05.48\n\t*\t")}))
           
           '({:line "22", :destination "Gistrup Skole", :time {:hour 0, :minute 29, :offset 0}, :reliable? true}
             {:line "22", :destination "Mølholm",       :time {:hour 0, :minute 53, :offset 2}, :reliable? true}
             {:line "2", :destination "Væddeløbsbanen", :time {:hour 5, :minute 48, :offset 0}, :reliable? false})))))
